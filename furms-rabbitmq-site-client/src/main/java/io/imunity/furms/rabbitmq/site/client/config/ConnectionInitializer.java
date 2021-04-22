/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.config;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.client.SiteAgentListenerConnector;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getSitePublishQueueName;


@Component
class ConnectionInitializer implements CommandLineRunner {
	private final SiteExternalIdsResolver siteExternalIdsResolver;
	private final SiteAgentListenerConnector siteAgentListenerConnector;
	private final RabbitAdmin rabbitAdmin;

	ConnectionInitializer(SiteExternalIdsResolver siteExternalIdsResolver, SiteAgentListenerConnector siteAgentListenerConnector, RabbitAdmin rabbitAdmin) {
		this.siteExternalIdsResolver = siteExternalIdsResolver;
		this.siteAgentListenerConnector = siteAgentListenerConnector;
		this.rabbitAdmin = rabbitAdmin;
	}

	@Override
	public void run(String... args) {
		for (SiteExternalId id : siteExternalIdsResolver.findAllIds()) {
			rabbitAdmin.declareQueue(new Queue(getFurmsPublishQueueName(id)));
			rabbitAdmin.declareQueue(new Queue(getSitePublishQueueName(id)));
			siteAgentListenerConnector.connectListenerToQueue(getSitePublishQueueName(id));
		}
	}
}
