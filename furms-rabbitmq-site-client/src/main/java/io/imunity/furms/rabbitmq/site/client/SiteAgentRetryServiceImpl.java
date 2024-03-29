/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.client.config.PlaneRabbitTemplate;
import io.imunity.furms.site.api.site_agent.SiteAgentRetryService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;

@Service
class SiteAgentRetryServiceImpl implements SiteAgentRetryService {
	private final PlaneRabbitTemplate rabbitTemplate;

	SiteAgentRetryServiceImpl(PlaneRabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void retry(SiteExternalId id, String json) {
		String queueName = getFurmsPublishQueueName(id);
		rabbitTemplate.send(queueName, new Message(json.getBytes(StandardCharsets.UTF_8), new MessageProperties()));
	}
}
