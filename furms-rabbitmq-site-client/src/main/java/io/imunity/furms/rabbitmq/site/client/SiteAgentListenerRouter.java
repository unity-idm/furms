/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.AgentPingResult;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectInstallationResult;
import io.imunity.furms.rabbitmq.site.models.consts.Queues;

@Component
@RabbitListener(queues = Queues.REPLY_QUEUE)
class SiteAgentListenerRouter {

	private final SiteAgentStatusServiceImpl siteAgentStatusService;
	private final SiteAgentProjectInstallationServiceImpl siteAgentProjectInstallationService;

	SiteAgentListenerRouter(SiteAgentStatusServiceImpl siteAgentStatusService, SiteAgentProjectInstallationServiceImpl siteAgentProjectInstallationService) {
		this.siteAgentStatusService = siteAgentStatusService;
		this.siteAgentProjectInstallationService = siteAgentProjectInstallationService;
	}

	@RabbitHandler
	public void receive(AgentPingAck ack) {
		siteAgentStatusService.receive(ack);
	}

	@RabbitHandler
	public void receive(AgentPingResult result) {
		siteAgentStatusService.receive(result);
	}

	@RabbitHandler
	public void receive(AgentProjectInstallationAck ack) {
		siteAgentProjectInstallationService.receive(ack);
	}

	@RabbitHandler
	public void receive(AgentProjectInstallationResult result, @Headers Map<String,Object> headers) {
		siteAgentProjectInstallationService.receive(result, headers);
	}
}
