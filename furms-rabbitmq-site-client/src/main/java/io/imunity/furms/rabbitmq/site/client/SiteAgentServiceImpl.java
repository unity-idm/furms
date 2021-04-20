/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.stereotype.Service;

import static io.imunity.furms.rabbitmq.site.client.SiteAgentListenerRouter.*;

@Service
class SiteAgentServiceImpl implements SiteAgentService {

	private final RabbitAdmin rabbitAdmin;
	private final RabbitListenerEndpointRegistry endpointRegistry;

	SiteAgentServiceImpl(RabbitAdmin rabbitAdmin, RabbitListenerEndpointRegistry endpointRegistry) {
		this.rabbitAdmin = rabbitAdmin;
		this.endpointRegistry = endpointRegistry;
	}

	@Override
	public void initializeSiteConnection(SiteExternalId externalId) {
		try {
			rabbitAdmin.declareQueue(new Queue(externalId.id + PUB_FURMS));
			rabbitAdmin.declareQueue(new Queue(externalId.id + PUB_SITE));
			AbstractMessageListenerContainer container = (AbstractMessageListenerContainer)endpointRegistry.getListenerContainer(FURMS_LISTENER);
//			container.addQueueNames(externalId.id + PUB_SITE);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void removeSiteConnection(SiteExternalId externalId) {
		try {
			rabbitAdmin.deleteQueue(externalId.id + PUB_FURMS);
			rabbitAdmin.deleteQueue(externalId.id + PUB_SITE);
			AbstractMessageListenerContainer container = (AbstractMessageListenerContainer)endpointRegistry.getListenerContainer(FURMS_LISTENER);
			container.removeQueueNames(externalId.id + PUB_SITE);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
