/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.rabbitmq.site.models.Error;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationStatusUpdater;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.*;
import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

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
			projectAllocationInstallationStatusUpdater.updateStatus(correlationId, ACKNOWLEDGED, Optional.empty());
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
			projectAllocationInstallationStatusUpdater.updateStatus(new CorrelationId(ack.header.messageCorrelationId), ProjectDeallocationStatus.ACKNOWLEDGED, Optional.empty());
		else
			projectAllocationInstallationStatusUpdater.updateStatus(
				correlationId,
				ProjectDeallocationStatus.FAILED,
				getErrorMessage(ack.header.error)
			);
	}

	@EventListener
	void receiveProjectResourceAllocationResult(Payload<AgentProjectAllocationInstallationResult> result) {
		if(result.header.status.equals(Status.OK)) {
			ProjectAllocationInstallation installation = ProjectAllocationInstallation.builder()
				.correlationId(new CorrelationId(result.header.messageCorrelationId))
				.projectAllocationId(result.body.allocationIdentifier)
				.chunkId(result.body.allocationChunkIdentifier)
				.amount(BigDecimal.valueOf(result.body.amount))
				.validFrom(convertToUTCTime(result.body.validFrom))
				.validTo(convertToUTCTime(result.body.validTo))
				.receivedTime(convertToUTCTime(result.body.receivedTime))
				.status(INSTALLED)
				.errorMessage(Optional.empty())
				.build();
			projectAllocationInstallationStatusUpdater.updateStatus(installation);
		}
		else {
			ProjectAllocationInstallation installation = ProjectAllocationInstallation.builder()
				.correlationId(new CorrelationId(result.header.messageCorrelationId))
				.status(FAILED)
				.errorMessage(getErrorMessage(result.header.error))
				.build();
			projectAllocationInstallationStatusUpdater.updateStatus(installation);
		}
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
