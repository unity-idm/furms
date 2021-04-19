/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.rabbitmq.site.models.AgentProjectResourceAllocationAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectResourceAllocationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectResourceAllocationResult;
import io.imunity.furms.rabbitmq.site.models.converter.TypeHeaderAppender;
import io.imunity.furms.site.api.message_resolver.ProjectAllocationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectAllocationService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Map;
import java.util.UUID;

import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.ACK;
import static io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus.DONE;

class SiteAgentProjectAllocationServiceImpl implements SiteAgentProjectAllocationService {
	private final RabbitTemplate rabbitTemplate;
	private final ProjectAllocationMessageResolver projectAllocationMessageResolver;

	SiteAgentProjectAllocationServiceImpl(RabbitTemplate rabbitTemplate, ProjectAllocationMessageResolver projectAllocationMessageResolver) {
		this.rabbitTemplate = rabbitTemplate;
		this.projectAllocationMessageResolver = projectAllocationMessageResolver;
	}

	void receive(AgentProjectResourceAllocationAck ack) {
		projectAllocationMessageResolver.updateStatus(new CorrelationId(ack.correlationId), ACK);
	}

	void receive(AgentProjectResourceAllocationResult result, Map<String,Object> headers) {
		ProjectAllocationInstallation installation = ProjectAllocationInstallation.builder()
			.correlationId(headers.get("correlationId").toString())
			.projectAllocationId(result.allocationIdentifier)
			.chunkId(result.allocationChunkIdentifier)
			.status(DONE)
			.build();
		projectAllocationMessageResolver.updateStatus(installation);
	}

	@Override
	public CorrelationId allocateProject(ProjectAllocationResolved projectAllocation) {
		String correlationId = UUID.randomUUID().toString();
		AgentProjectResourceAllocationRequest request = ProjectAllocationMapper.map(projectAllocation);
		String queueName = projectAllocation.site.getExternalId().id;
		try {
			rabbitTemplate.convertAndSend(queueName, request, new TypeHeaderAppender(request, correlationId));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
		return new CorrelationId(correlationId);
	}
}
