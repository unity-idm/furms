/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemoval;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.message_resolver.UserOperationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static java.util.Collections.emptyList;

@Service
class SiteAgentUserServiceImpl implements SiteAgentUserService {
	private final RabbitTemplate rabbitTemplate;
	private final UserOperationMessageResolver userOperationMessageResolver;

	SiteAgentUserServiceImpl(RabbitTemplate rabbitTemplate, UserOperationMessageResolver userOperationMessageResolver) {
		this.rabbitTemplate = rabbitTemplate;
		this.userOperationMessageResolver = userOperationMessageResolver;
	}

	@EventListener
	void receiveUserProjectAddRequestAck(Payload<UserProjectAddRequestAck> ack) {
		CorrelationId correlationId = new CorrelationId(ack.header.messageCorrelationId);
		if(ack.header.status.equals(Status.OK))
			userOperationMessageResolver.updateStatus(correlationId, UserAdditionStatus.ACKNOWLEDGED);
		else
			userOperationMessageResolver.updateStatus(correlationId, UserAdditionStatus.FAILED);
	}

	@EventListener
	void receiveUserProjectAddResult(Payload<UserProjectAddResult> result) {
		UserAdditionStatus status;
		if(result.header.status.equals(Status.OK))
			status = UserAdditionStatus.ADDED;
		else
			status = UserAdditionStatus.FAILED;
		userOperationMessageResolver.update(
			UserAddition.builder()
				.correlationId(new CorrelationId(result.header.messageCorrelationId))
				.userId(result.body.uid)
				.status(status)
				.build()
		);
	}

	@EventListener
	void receiveUserProjectRemovalRequestAck(Payload<UserProjectRemovalRequestAck> ack) {
		CorrelationId correlationId = new CorrelationId(ack.header.messageCorrelationId);
		if(ack.header.status.equals(Status.OK))
			userOperationMessageResolver.updateStatus(correlationId, UserRemovalStatus.ACKNOWLEDGED);
		else
			userOperationMessageResolver.updateStatus(correlationId, UserRemovalStatus.FAILED);
	}

	@EventListener
	void receiveUserProjectRemovalResult(Payload<UserProjectRemovalResult> result) {
		CorrelationId correlationId = new CorrelationId(result.header.messageCorrelationId);
		if(result.header.status.equals(Status.OK))
			userOperationMessageResolver.updateStatus(correlationId, UserRemovalStatus.REMOVED);
		else
			userOperationMessageResolver.updateStatus(correlationId, UserRemovalStatus.FAILED);
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
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
	@Transactional(propagation = Propagation.NESTED)
	public void removeUser(UserRemoval userRemoval) {
		try {
			rabbitTemplate.convertAndSend(
				getFurmsPublishQueueName(userRemoval.siteId.externalId),
				new Payload<>(
					new Header(VERSION, userRemoval.correlationId.id),
					new UserProjectRemovalRequest(userRemoval.userId, userRemoval.projectId)
				)
			);
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
