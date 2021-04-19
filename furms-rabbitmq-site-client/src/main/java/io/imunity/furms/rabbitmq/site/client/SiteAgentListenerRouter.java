/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.rabbitmq.site.models.consts.Queues;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = Queues.REPLY_QUEUE)
class SiteAgentListenerRouter {

	private final SiteAgentStatusServiceImpl siteAgentStatusService;
	private final SiteAgentProjectInstallationServiceImpl siteAgentProjectInstallationService;
	private final SiteAgentProjectAllocationServiceImpl siteAgentProjectAllocationService;

	SiteAgentListenerRouter(SiteAgentStatusServiceImpl siteAgentStatusService,
	                        SiteAgentProjectInstallationServiceImpl siteAgentProjectInstallationService,
	                        SiteAgentProjectAllocationServiceImpl siteAgentProjectAllocationService) {
		this.siteAgentStatusService = siteAgentStatusService;
		this.siteAgentProjectInstallationService = siteAgentProjectInstallationService;
		this.siteAgentProjectAllocationService = siteAgentProjectAllocationService;
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

	@RabbitHandler
	public void receive(AgentProjectResourceAllocationAck ack) {
		siteAgentProjectAllocationService.receive(ack);
	}

	@RabbitHandler
	public void receive(AgentProjectResourceAllocationResult result, @Headers Map<String,Object> headers) {
		siteAgentProjectAllocationService.receive(result, headers);
	}
}
