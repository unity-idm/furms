/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.rabbitmq.site.client.message_authorizer.MessageAuthorizer;
import io.imunity.furms.rabbitmq.site.models.Payload;
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
import java.util.List;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getSiteId;
import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.FURMS_LISTENER;

@Component
@RabbitListener(id = FURMS_LISTENER)
class SiteAgentListenerRouter {

	public static final String FURMS_LISTENER = "FURMS_LISTENER";
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ApplicationEventPublisher publisher;
	private final List<MessageAuthorizer> messageAuthorizers;

	SiteAgentListenerRouter(ApplicationEventPublisher publisher, List<MessageAuthorizer> messageAuthorizers) {
		this.publisher = publisher;
		this.messageAuthorizers = messageAuthorizers;
	}

	@RabbitHandler
	public void receive(Payload<?> payload, @Header("amqp_receivedRoutingKey") String receivedRoutingKey, @Header("amqp_consumerQueue") String queueName) {
		messageAuthorizers.stream()
			.filter(messageAuthorizer -> messageAuthorizer.isApplicable(payload.body.getClass()))
			.filter(messageAuthorizer -> messageAuthorizer.isValidate(getSiteId(queueName), payload))
			.findAny()
			.orElseThrow(() -> new IllegalArgumentException(String.format("Error correlation id %s doesn't exist", payload.header.messageCorrelationId)));

		MDC.put(MDCKey.QUEUE_NAME.key, receivedRoutingKey);
		try {
			publisher.publishEvent(payload);
			LOG.info("Received payload {}", payload);
		} catch (Exception e) {
			LOG.error("This error occurred while processing payload: {}", payload, e);
		} finally {
			MDC.remove(MDCKey.QUEUE_NAME.key);
		}
	}

	@RabbitHandler(isDefault = true)
	public void receive(Object o) {
		LOG.info("Received object, which cannot be processed {}", o);
	}

}
