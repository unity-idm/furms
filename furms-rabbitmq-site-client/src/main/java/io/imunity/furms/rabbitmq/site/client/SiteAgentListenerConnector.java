/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.FURMS_LISTENER;

import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Component
public class SiteAgentListenerConnector {
	private final RabbitListenerEndpointRegistry endpointRegistry;

	SiteAgentListenerConnector(RabbitListenerEndpointRegistry endpointRegistry) {
		this.endpointRegistry = endpointRegistry;
	}

	public void connectListenerToQueue(String queueName) {
		AbstractMessageListenerContainer furmsContainer = (AbstractMessageListenerContainer) endpointRegistry
				.getListenerContainer(FURMS_LISTENER);
		furmsContainer.addQueueNames(queueName);
	}

	public void disconnectListenerToQueue(String queueName) {
		AbstractMessageListenerContainer furmsContainer = (AbstractMessageListenerContainer) endpointRegistry
				.getListenerContainer(FURMS_LISTENER);
		furmsContainer.removeQueueNames(queueName);
	}
}
