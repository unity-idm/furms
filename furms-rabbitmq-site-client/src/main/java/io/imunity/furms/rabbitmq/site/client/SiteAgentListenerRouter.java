/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.rabbitmq.site.models.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.FURMS_LISTENER;

@Component
@RabbitListener(id = FURMS_LISTENER)
class SiteAgentListenerRouter {

	public static final String FURMS_LISTENER = "FURMS_LISTENER";
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ApplicationEventPublisher publisher;

	SiteAgentListenerRouter(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@RabbitHandler
	public void receive(Payload<?> payload) {
		publisher.publishEvent(payload);
	}

	@RabbitHandler(isDefault = true)
	public void receive(Object o) {
		LOG.info("Received object, which cannot be process {}", o);
	}

	@EventListener
	public void receiveAgentProjectResourceAllocationAck(Payload<AgentProjectAllocationInstallationAck> ack) {
		siteAgentProjectAllocationService.receiveProjectResourceAllocationAck(ack);
	}

	@EventListener
	public void receiveAgentProjectResourceAllocationResult(Payload<AgentProjectAllocationInstallationResult> result) {
		siteAgentProjectAllocationService.receiveProjectResourceAllocationResult(result);
	}
}
