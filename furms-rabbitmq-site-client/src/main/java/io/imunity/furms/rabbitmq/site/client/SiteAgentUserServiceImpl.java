/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.user_addition.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static java.util.Collections.emptyList;

@Service
class SiteAgentUserServiceImpl implements SiteAgentUserService {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired
	private SiteExternalIdsResolver siteExternalIdsResolver;

	@EventListener
	void receiveUserProjectAddRequestAck(Payload<UserProjectAddRequestAck> ack) {
	}

	@EventListener
	void receiveUserProjectAddResult(Payload<UserProjectAddResult> result) {
	}

	@EventListener
	void receiveUserProjectRemovalRequestAck(Payload<UserProjectRemovalRequestAck> ack) {
	}

	@EventListener
	void receiveUserProjectRemovalResult(Payload<UserProjectRemovalResult> result) {
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public void addUser(CorrelationId correlationId, List<SiteExternalId> ids, FURMSUser user, String projectId) {
		AgentUser agentUser = UserMapper.map(user);
		try {
			ids.forEach(id ->
				rabbitTemplate.convertAndSend(
					getFurmsPublishQueueName(id),
					new Payload<>(new Header(VERSION, correlationId.id), new UserProjectAddRequest(agentUser, emptyList(), projectId))
			));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public void removeUser(CorrelationId correlationId, List<UserAddition> userAdditions) {
		try {
			userAdditions.forEach(userAddition ->
				rabbitTemplate.convertAndSend(
					getFurmsPublishQueueName( siteExternalIdsResolver.translate(userAddition.siteId).get()),
					new Payload<>(
						new Header(VERSION, correlationId.id),
						new UserProjectRemovalRequest(userAddition.userId, userAddition.uid, userAddition.projectId)
					)
			));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
