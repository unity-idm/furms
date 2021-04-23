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
import org.springframework.stereotype.Service;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getSitePublishQueueName;

@Service
class SiteAgentServiceImpl implements SiteAgentService {

	private final RabbitAdmin rabbitAdmin;
	private final SiteAgentListenerConnector siteAgentListenerConnector;

	SiteAgentServiceImpl(RabbitAdmin rabbitAdmin, SiteAgentListenerConnector siteAgentListenerConnector) {
		this.rabbitAdmin = rabbitAdmin;
		this.siteAgentListenerConnector = siteAgentListenerConnector;
	}

	@Override
	public void initializeSiteConnection(SiteExternalId externalId) {
		try {
			rabbitAdmin.declareQueue(new Queue(getFurmsPublishQueueName(externalId)));
			rabbitAdmin.declareQueue(new Queue(getSitePublishQueueName(externalId)));
			siteAgentListenerConnector.connectListenerToQueue(getSitePublishQueueName(externalId));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void removeSiteConnection(SiteExternalId externalId) {
		try {
			rabbitAdmin.deleteQueue(getFurmsPublishQueueName(externalId));
			rabbitAdmin.deleteQueue(getSitePublishQueueName(externalId));
			siteAgentListenerConnector.disconnectListenerToQueue(getSitePublishQueueName(externalId));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
