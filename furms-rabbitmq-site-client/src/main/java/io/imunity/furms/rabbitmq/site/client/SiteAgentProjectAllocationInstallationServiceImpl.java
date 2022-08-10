/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.site_agent.UnsupportedFailedChunkException;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationResult;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationUpdate;
import io.imunity.furms.rabbitmq.site.models.AgentProjectDeallocationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectDeallocationRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectResourceAllocationStatusResult;
import io.imunity.furms.rabbitmq.site.models.Error;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Status;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import io.imunity.furms.site.api.status_updater.ProjectAllocationInstallationStatusUpdater;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.FAILED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.INSTALLED;
import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;
import static java.util.Optional.empty;

@Service
class SiteAgentProjectAllocationInstallationServiceImpl implements SiteAgentProjectAllocationInstallationService {
	private final RabbitTemplate rabbitTemplate;
	private final ProjectAllocationInstallationStatusUpdater projectAllocationInstallationStatusUpdater;

	SiteAgentProjectAllocationInstallationServiceImpl(RabbitTemplate rabbitTemplate, ProjectAllocationInstallationStatusUpdater projectAllocationInstallationStatusUpdater) {
		this.rabbitTemplate = rabbitTemplate;
		this.projectAllocationInstallationStatusUpdater = projectAllocationInstallationStatusUpdater;
	}

	@EventListener
	void receiveProjectResourceAllocationAck(Payload<AgentProjectAllocationInstallationAck> ack) {
		CorrelationId correlationId = new CorrelationId(ack.header.messageCorrelationId);
		if(ack.header.status.equals(Status.OK))
			projectAllocationInstallationStatusUpdater.updateStatusToAck(correlationId);
		else
			projectAllocationInstallationStatusUpdater.updateStatus(
				correlationId,
				FAILED,
				getErrorMessage(ack.header.error)
			);
	}

	@EventListener
	void receiveProjectResourceAllocationStatusResult(Payload<AgentProjectResourceAllocationStatusResult> ack) {
		CorrelationId correlationId = new CorrelationId(ack.header.messageCorrelationId);
		if(ack.header.status.equals(Status.OK))
			projectAllocationInstallationStatusUpdater.updateStatus(correlationId, INSTALLED, Optional.empty());
		else
			projectAllocationInstallationStatusUpdater.updateStatus(
				correlationId,
				FAILED,
				getErrorMessage(ack.header.error)
			);
	}

	@EventListener
	void receiveProjectResourceDeallocationAck(Payload<AgentProjectDeallocationRequestAck> ack) {
		CorrelationId correlationId = new CorrelationId(ack.header.messageCorrelationId);
		if(ack.header.status.equals(Status.OK))
			projectAllocationInstallationStatusUpdater.updateStatus(new CorrelationId(ack.header.messageCorrelationId), ProjectDeallocationStatus.ACKNOWLEDGED, empty());
		else
			projectAllocationInstallationStatusUpdater.updateStatus(
				correlationId,
				ProjectDeallocationStatus.FAILED,
				getErrorMessage(ack.header.error)
			);
	}

	@EventListener
	void receiveProjectResourceAllocationResult(Payload<AgentProjectAllocationInstallationResult> result) {
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(result.body.allocationIdentifier);
		boolean isStateUpdatable = projectAllocationInstallationStatusUpdater.isWaitingForInstallationConfirmation(projectAllocationId);
		if(isStateUpdatable) {
			if(Status.FAILED.equals(result.header.status)) {
				projectAllocationInstallationStatusUpdater.updateStatus(
					projectAllocationId,
					FAILED,
					getErrorMessage(result.header.error)
				);
				return;
			}
			projectAllocationInstallationStatusUpdater.updateStatus(projectAllocationId, INSTALLED, empty());
		}
		if(Status.FAILED.equals(result.header.status))
			throw new UnsupportedFailedChunkException(String.format("Subsequent allocation %s chunk can not be sent " +
				"with a failed status", result.body.allocationIdentifier));

		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId(result.body.allocationIdentifier)
			.chunkId(result.body.allocationChunkIdentifier)
			.amount(result.body.amount)
			.validFrom(convertToUTCTime(result.body.validFrom))
			.validTo(convertToUTCTime(result.body.validTo))
			.receivedTime(convertToUTCTime(result.body.receivedTime))
			.build();
		projectAllocationInstallationStatusUpdater.createChunk(chunk);
	}

	@EventListener
	void receiveProjectResourceAllocationUpdate(Payload<AgentProjectAllocationUpdate> result) {
		ProjectAllocationId projectAllocationId = new ProjectAllocationId(result.body.allocationIdentifier);
		boolean isStateUpdatable = projectAllocationInstallationStatusUpdater.isWaitingForInstallationConfirmation(projectAllocationId);
		if(isStateUpdatable) {
			if(Status.FAILED.equals(result.header.status)) {
				projectAllocationInstallationStatusUpdater.updateStatus(
					projectAllocationId,
					FAILED,
					getErrorMessage(result.header.error)
				);
				return;
			}
			projectAllocationInstallationStatusUpdater.updateStatus(projectAllocationId, INSTALLED, empty());
		}

		if(Status.FAILED.equals(result.header.status))
			throw new UnsupportedFailedChunkException(String.format("Update of chunk of allocation %s can not be set " +
				"to failed", result.body.allocationIdentifier));

		ProjectAllocationChunk chunk = ProjectAllocationChunk.builder()
			.projectAllocationId(result.body.allocationIdentifier)
			.chunkId(result.body.allocationChunkIdentifier)
			.amount(result.body.amount)
			.validFrom(convertToUTCTime(result.body.validFrom))
			.validTo(convertToUTCTime(result.body.validTo))
			.receivedTime(convertToUTCTime(result.body.receivedTime))
			.build();
		projectAllocationInstallationStatusUpdater.updateChunk(chunk);
	}

	private Optional<ErrorMessage> getErrorMessage(Error error) {
		return Optional.ofNullable(error).map(x -> new ErrorMessage(x.code, x.message));
	}

	@Override
	public void allocateProject(CorrelationId correlationId, ProjectAllocationResolved projectAllocation) {
		AgentProjectAllocationInstallationRequest request = ProjectAllocationMapper.mapAllocation(projectAllocation);
		try {
			String queueName = getFurmsPublishQueueName(projectAllocation.site.getExternalId());
			rabbitTemplate.convertAndSend(queueName, new Payload<>(new Header(VERSION, correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void deallocateProject(CorrelationId correlationId, ProjectAllocationResolved projectAllocation) {
		AgentProjectDeallocationRequest request = ProjectAllocationMapper.mapDeallocation(projectAllocation);
		try {
			String queueName = getFurmsPublishQueueName(projectAllocation.site.getExternalId());
			rabbitTemplate.convertAndSend(queueName, new Payload<>(new Header(VERSION, correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
