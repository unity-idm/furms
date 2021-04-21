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
import org.springframework.stereotype.Service;

import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.ACK;
import static io.imunity.furms.domain.project_installation.ProjectInstallationStatus.DONE;
import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.PUB_FURMS;

@Service
class SiteAgentProjectInstallationServiceImpl implements SiteAgentProjectInstallationService {
	private final RabbitTemplate rabbitTemplate;
	private final ProjectInstallationMessageResolver projectInstallationService;

	SiteAgentProjectInstallationServiceImpl(RabbitTemplate rabbitTemplate, ProjectInstallationMessageResolver projectInstallationService) {
		this.rabbitTemplate = rabbitTemplate;
		this.projectInstallationService = projectInstallationService;
	}

	void receiveAgentProjectInstallationAck(Payload<AgentProjectInstallationAck> ack) {
		projectInstallationService.updateStatus(new CorrelationId(ack.header.messageCorrelationId), ACK);
	}

	void receiveAgentProjectInstallationResult(Payload<AgentProjectInstallationResult> result) {
		String correlationId = result.header.messageCorrelationId;
		projectInstallationService.updateStatus(new CorrelationId(correlationId), DONE);
	}

	@Override
	public void installProject(CorrelationId correlationId, ProjectInstallation installation) {
		AgentProjectInstallationRequest request = ProjectInstallationMapper.map(installation);
		try {
			rabbitTemplate.convertAndSend(installation.siteExternalId + PUB_FURMS, new Payload<>(new Header("1", correlationId.id), request));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
