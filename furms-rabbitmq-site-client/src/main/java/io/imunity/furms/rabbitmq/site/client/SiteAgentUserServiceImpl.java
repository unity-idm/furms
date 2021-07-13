/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.AgentUser;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddRequest;
import io.imunity.furms.rabbitmq.site.models.UserProjectRemovalRequest;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static java.util.Collections.emptyList;

@Service
class SiteAgentUserServiceImpl implements SiteAgentUserService {
	private final RabbitTemplate rabbitTemplate;

	SiteAgentUserServiceImpl(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@Override
	public void addUser(UserAddition userAddition, FURMSUser user) {
		AgentUser agentUser = UserMapper.map(user);
		try {
			UserProjectAddRequest body = new UserProjectAddRequest(agentUser, emptyList(), userAddition.projectId);
			rabbitTemplate.convertAndSend(
				getFurmsPublishQueueName(userAddition.siteId.externalId),
				new Payload<>(new Header(VERSION, userAddition.correlationId.id), body)
			);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	public void removeUser(UserAddition userAddition) {
		try {
			rabbitTemplate.convertAndSend(
				getFurmsPublishQueueName(userAddition.siteId.externalId),
				new Payload<>(
					new Header(VERSION, userAddition.correlationId.id),
					new UserProjectRemovalRequest(userAddition.userId, userAddition.projectId)
				)
			);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
