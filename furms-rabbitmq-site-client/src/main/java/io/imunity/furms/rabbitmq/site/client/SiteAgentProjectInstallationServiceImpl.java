/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.project_installation.ProjectInstallation;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.message_resolver.ProjectInstallationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentProjectInstallationService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.*;
import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Service
class SiteAgentProjectInstallationServiceImpl implements SiteAgentProjectInstallationService {
	private final RabbitTemplate rabbitTemplate;
	private final ProjectInstallationMessageResolver projectInstallationService;

	SiteAgentProjectInstallationServiceImpl(RabbitTemplate rabbitTemplate, ProjectInstallationMessageResolver projectInstallationService) {
		this.rabbitTemplate = rabbitTemplate;
		this.projectInstallationService = projectInstallationService;
	}

	@EventListener
	void receiveAgentProjectInstallationAck(Payload<AgentProjectInstallationAck> ack) {
		if(ack.header.status.equals(Status.FAILED)){
			projectInstallationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), FAILED);
		}
		projectInstallationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), ACKNOWLEDGED);
	}

	@EventListener
	void receiveAgentProjectInstallationResult(Payload<AgentProjectInstallationResult> result) {
		String correlationId = result.header.messageCorrelationId;
		if(result.header.status.equals(Status.FAILED)){
			projectInstallationService.updateStatus(new CorrelationId(correlationId), FAILED);
		}
		projectInstallationService.updateStatus(new CorrelationId(correlationId), INSTALLED);
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public void installProject(CorrelationId correlationId, ProjectInstallation installation) {
		AgentProjectInstallationRequest request = ProjectInstallationMapper.map(installation);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(installation.siteExternalId), new Payload<>(new Header(VERSION, correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
