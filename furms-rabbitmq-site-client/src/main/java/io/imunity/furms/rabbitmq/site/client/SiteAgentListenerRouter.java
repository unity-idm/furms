/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.rabbitmq.site.models.*;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SiteAgentListenerRouter {

	public static final String FURMS_LISTENER = "FURMS_LISTENER";
	public static final String PUB_FURMS = "-pub-furms";
	public static final String PUB_SITE = "-pub-site";
	private final SiteAgentStatusServiceImpl siteAgentStatusService;
	private final SiteAgentProjectInstallationServiceImpl siteAgentProjectInstallationService;
	private final ApplicationEventPublisher publisher;

	SiteAgentListenerRouter(SiteAgentStatusServiceImpl siteAgentStatusService,
	                        SiteAgentProjectInstallationServiceImpl siteAgentProjectInstallationService,
	                        ApplicationEventPublisher publisher) {
		this.siteAgentStatusService = siteAgentStatusService;
		this.siteAgentProjectInstallationService = siteAgentProjectInstallationService;
		this.publisher = publisher;
	}

	@RabbitHandler
	@RabbitListener(id = FURMS_LISTENER)
	public void receive(Payload<?> payload) {
		publisher.publishEvent(payload);
	}

	@RabbitHandler
	@RabbitListener(id = "kumpaczos")
	public void receive2(Object o) {
	}

	@EventListener
	public void receiveAgentPingAck(Payload<AgentPingAck> ack) {
		siteAgentStatusService.receiveAgentPingAck(ack);
	}

	@EventListener
	public void receiveAgentProjectInstallationAck(Payload<AgentProjectInstallationAck> ack) {
		siteAgentProjectInstallationService.receiveAgentProjectInstallationAck(ack);
	}

	@EventListener
	public void receiveAgentProjectInstallationResult(Payload<AgentProjectInstallationResult> result) {
		siteAgentProjectInstallationService.receiveAgentProjectInstallationResult(result);
	}
}
