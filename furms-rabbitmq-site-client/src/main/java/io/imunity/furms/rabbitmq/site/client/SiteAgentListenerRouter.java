/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.rabbitmq.site.models.Ack;
import io.imunity.furms.rabbitmq.site.models.AgentProjectAllocationInstallationAck;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.Result;
import io.imunity.furms.site.api.SiteAgentPendingMessageResolver;
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
	private final SiteAgentPendingMessageResolver repository;

	SiteAgentListenerRouter(ApplicationEventPublisher publisher, MessageAuthorizer validator, SiteAgentPendingMessageResolver repository) {
		this.publisher = publisher;
		this.validator = validator;
		this.repository = repository;
	}

	@RabbitHandler
	public void receive(Payload<?> payload, @Header("amqp_consumerQueue") String queueName) {
		MDC.put(MDCKey.QUEUE_NAME.key, queueName);
		try {
			validator.validate(payload, queueName);
			publisher.publishEvent(payload);
			menagePendingRequests(payload);
			LOG.info("Received payload {}", payload);
		} catch (Exception e) {
			LOG.error("This error occurred while processing payload: {} from queue {}", payload, queueName, e);
		} finally {
			MDC.remove(MDCKey.QUEUE_NAME.key);
		}
	}

	private void menagePendingRequests(Payload<?> payload) {
		if(payload.header.status.equals(OK) && payload.body instanceof Ack)
			repository.updateToAck(new CorrelationId(payload.header.messageCorrelationId));
		if((payload.header.status.equals(OK) && payload.body instanceof Result) || payload.body instanceof AgentProjectAllocationInstallationAck)
			repository.delete(new CorrelationId(payload.header.messageCorrelationId));
	}

	@RabbitHandler(isDefault = true)
	public void receive(Object o) {
		LOG.info("Received object, which cannot be processed {}", o);
	}

}
