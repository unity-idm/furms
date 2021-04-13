/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationResult;
import io.imunity.furms.rabbitmq.site.models.converter.TypeHeaderAppender;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectInstallationService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACK;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.DONE;
import static io.imunity.furms.rabbitmq.site.client.SiteAgentProjectInstallationServiceImpl.REPLY_QUEUE;
import static io.imunity.furms.rabbitmq.site.models.consts.Headers.CORRELATION_ID;

@Component
@RabbitListener(queues = REPLY_QUEUE)
class SiteAgentProjectInstallationServiceImpl implements SiteAgentProjectInstallationService {
	static final String REPLY_QUEUE = "reply-queue";

	private final RabbitTemplate rabbitTemplate;
	private final ProjectInstallationMessageResolver projectInstallationService;

	SiteAgentProjectInstallationServiceImpl(RabbitTemplate rabbitTemplate, ProjectInstallationMessageResolver projectInstallationService) {
		this.rabbitTemplate = rabbitTemplate;
		this.projectInstallationService = projectInstallationService;
	}

	@RabbitHandler
	public void receive(AgentProjectInstallationAck ack) {
		projectInstallationService.updateStatus(new CorrelationId(ack.correlationId), ACK);
	}

	@RabbitHandler
	public void receive(AgentProjectInstallationResult result, @Headers Map<String,Object> headers) {
		String correlationId = headers.get(CORRELATION_ID).toString();
		projectInstallationService.updateStatus(new CorrelationId(correlationId), DONE);
	}

	@Override
	public CorrelationId installProject(ProjectInstallation installation) {
		String correlationId = UUID.randomUUID().toString();
		AgentProjectInstallationRequest request = ProjectInstallationMapper.map(installation);
		try {
			rabbitTemplate.convertAndSend(installation.siteExternalId, request, new TypeHeaderAppender(request, correlationId));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
		return new CorrelationId(correlationId);
	}
}
