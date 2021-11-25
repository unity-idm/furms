/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.rabbitmq.site.models.Ack;
import io.imunity.furms.rabbitmq.site.models.AgentPingAck;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationAck;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Result;
import io.imunity.furms.site.api.AgentPendingMessageSiteService;
import io.imunity.furms.utils.MDCKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.FURMS_LISTENER;
import static io.imunity.furms.rabbitmq.site.models.Status.OK;

@Component
@RabbitListener(id = FURMS_LISTENER)
class SiteAgentListenerRouter {

	static final String FURMS_LISTENER = "FURMS_LISTENER";
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ApplicationEventPublisher publisher;
	private final MessageAuthorizer validator;
	private final AgentPendingMessageSiteService agentPendingMessageSiteService;

	SiteAgentListenerRouter(ApplicationEventPublisher publisher, MessageAuthorizer validator, AgentPendingMessageSiteService agentPendingMessageSiteService) {
		this.publisher = publisher;
		this.validator = validator;
		this.agentPendingMessageSiteService = agentPendingMessageSiteService;
	}

	@RabbitHandler
	public void receive(Payload<?> payload, @Header("amqp_consumerQueue") String queueName) {
		MDC.put(MDCKey.QUEUE_NAME.key, queueName);
		try {
			validator.validate(payload, queueName);
			publisher.publishEvent(payload);
			updateOrDeletePendingRequests(payload);
			LOG.info("Received payload {}", payload);
		} catch (Exception e) {
			LOG.error("This error occurred while processing payload: {} from queue {}", payload, queueName, e);
		} finally {
			MDC.remove(MDCKey.QUEUE_NAME.key);
		}
	}

	/**
	 * This method update or delete pending message based on arriving message type.
	 * If message is Ack type it should be update, if message is Result type it should be delete.
	 * There are two exceptions when AgentProjectAllocationInstallationAck or AgentPingAck arrived, pending message should be removed,
	 * because project allocation and ping message kind don't have result type.
	 */
	private void updateOrDeletePendingRequests(Payload<?> payload) {
		if(payload.body instanceof AgentPingAck || payload.body instanceof AgentProjectAllocationInstallationAck)
			agentPendingMessageSiteService.delete(new CorrelationId(payload.header.messageCorrelationId));
		else if(payload.header.status.equals(OK) && payload.body instanceof Ack)
			agentPendingMessageSiteService.setAsAcknowledged(new CorrelationId(payload.header.messageCorrelationId));
		else if(payload.header.status.equals(OK) && payload.body instanceof Result)
			agentPendingMessageSiteService.delete(new CorrelationId(payload.header.messageCorrelationId));
	}

	@RabbitHandler(isDefault = true)
	public void receive(Object o) {
		LOG.info("Received object, which cannot be processed {}", o);
	}

}
