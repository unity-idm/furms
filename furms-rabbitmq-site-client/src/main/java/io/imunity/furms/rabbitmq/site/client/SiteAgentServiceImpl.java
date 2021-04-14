/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.site.api.site_agent.SiteAgentService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Service
class SiteAgentServiceImpl implements SiteAgentService {

	private final RabbitAdmin rabbitAdmin;

	SiteAgentServiceImpl(RabbitAdmin rabbitAdmin) {
		this.rabbitAdmin = rabbitAdmin;
	}

	@Override
	public void initializeSiteConnection(SiteExternalId externalId) {
		try {
			rabbitAdmin.declareQueue(new org.springframework.amqp.core.Queue(externalId.id));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void removeSiteConnection(SiteExternalId externalId) {
		try {
			rabbitAdmin.deleteQueue(externalId.id);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
