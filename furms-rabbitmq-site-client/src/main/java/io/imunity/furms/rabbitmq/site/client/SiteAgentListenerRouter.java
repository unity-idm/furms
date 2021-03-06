/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.FURMS_LISTENER;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.utils.MDCKey;

@Component
@RabbitListener(id = FURMS_LISTENER)
class SiteAgentListenerRouter {

	public static final String FURMS_LISTENER = "FURMS_LISTENER";
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ApplicationEventPublisher publisher;
	private final MessageAuthorizer validator;

	SiteAgentListenerRouter(ApplicationEventPublisher publisher, MessageAuthorizer messageAuthorizer) {
		this.publisher = publisher;
		this.validator = messageAuthorizer;
	}

	@RabbitHandler
	public void receive(Payload<?> payload, @Header("amqp_consumerQueue") String queueName) {
		MDC.put(MDCKey.QUEUE_NAME.key, queueName);
		try {
			validator.validate(payload, queueName);
			publisher.publishEvent(payload);
			LOG.info("Received payload {}", payload);
		} catch (Exception e) {
			LOG.error("This error occurred while processing payload: {} from queue {}", payload, queueName, e);
		} finally {
			MDC.remove(MDCKey.QUEUE_NAME.key);
		}
	}

	@RabbitHandler(isDefault = true)
	public void receive(Object o) {
		LOG.info("Received object, which cannot be processed {}", o);
	}

}
