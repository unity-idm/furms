/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationInstallationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationInstallationService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.ACKNOWLEDGED;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.INSTALLED;
import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCTime;

@Service
class SiteAgentProjectAllocationInstallationServiceImpl implements SiteAgentProjectAllocationInstallationService {
	private final RabbitTemplate rabbitTemplate;
	private final ProjectAllocationInstallationMessageResolver projectAllocationInstallationMessageResolver;

	SiteAgentProjectAllocationInstallationServiceImpl(RabbitTemplate rabbitTemplate, ProjectAllocationInstallationMessageResolver projectAllocationInstallationMessageResolver) {
		this.rabbitTemplate = rabbitTemplate;
		this.projectAllocationInstallationMessageResolver = projectAllocationInstallationMessageResolver;
	}

	@EventListener
	void receiveProjectResourceAllocationAck(Payload<AgentProjectAllocationInstallationAck> ack) {
		projectAllocationInstallationMessageResolver.updateStatus(new CorrelationId(ack.header.messageCorrelationId), ACKNOWLEDGED);
	}

	@EventListener
	void receiveProjectResourceAllocationResult(Payload<AgentProjectAllocationInstallationResult> result) {
		ProjectAllocationInstallation installation = ProjectAllocationInstallation.builder()
			.correlationId(new CorrelationId(result.header.messageCorrelationId))
			.projectAllocationId(result.body.allocationIdentifier)
			.chunkId(result.body.allocationChunkIdentifier)
			.amount(BigDecimal.valueOf(result.body.amount))
			.validFrom(convertToUTCTime(result.body.validFrom))
			.validTo(convertToUTCTime(result.body.validTo))
			.receivedTime(convertToUTCTime(result.body.receivedTime))
			.status(INSTALLED)
			.build();
		projectAllocationInstallationMessageResolver.updateStatus(installation);
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public void allocateProject(CorrelationId correlationId, ProjectAllocationResolved projectAllocation) {
		AgentProjectAllocationInstallationRequest request = ProjectAllocationMapper.map(projectAllocation);
		try {
			String queueName = getFurmsPublishQueueName(projectAllocation.site.getExternalId());
			rabbitTemplate.convertAndSend(queueName, new Payload<>(new Header(VERSION, correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
