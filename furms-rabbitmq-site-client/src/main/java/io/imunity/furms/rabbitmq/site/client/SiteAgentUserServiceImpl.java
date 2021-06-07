/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionErrorMessage;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.rabbitmq.site.models.Error;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.status_updater.UserOperationStatusUpdater;
import io.imunity.furms.site.api.site_agent.SiteAgentUserService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Service
class SiteAgentUserServiceImpl implements SiteAgentUserService {
	private final RabbitTemplate rabbitTemplate;
	private final UserOperationStatusUpdater userOperationStatusUpdater;

	SiteAgentUserServiceImpl(RabbitTemplate rabbitTemplate, UserOperationStatusUpdater userOperationStatusUpdater) {
		this.rabbitTemplate = rabbitTemplate;
		this.userOperationStatusUpdater = userOperationStatusUpdater;
	}

	@EventListener
	void receiveUserProjectAddRequestAck(Payload<UserProjectAddRequestAck> ack) {
		CorrelationId correlationId = new CorrelationId(ack.header.messageCorrelationId);
		if(ack.header.status.equals(Status.OK))
			userOperationStatusUpdater.updateStatus(correlationId, UserStatus.ADDING_ACKNOWLEDGED, Optional.empty());
		else
			userOperationStatusUpdater.updateStatus(correlationId, UserStatus.ADDING_FAILED, getErrorMessage(ack.header.error));
	}

	private Optional<UserAdditionErrorMessage> getErrorMessage(Error error) {
		return ofNullable(error).map(x -> new UserAdditionErrorMessage(x.code, x.message));
	}

	@EventListener
	void receiveUserProjectAddResult(Payload<UserProjectAddResult> result) {
		UserStatus status;
		if(result.header.status.equals(Status.OK))
			status = UserStatus.ADDED;
		else
			status = UserStatus.ADDING_FAILED;
		userOperationStatusUpdater.update(
			UserAddition.builder()
				.correlationId(new CorrelationId(result.header.messageCorrelationId))
				.uid(result.body.uid)
				.status(status)
				.errorMessage(getErrorMessage(result.header.error))
				.build()
		);
	}

	@EventListener
	void receiveUserProjectRemovalRequestAck(Payload<UserProjectRemovalRequestAck> ack) {
		CorrelationId correlationId = new CorrelationId(ack.header.messageCorrelationId);
		if(ack.header.status.equals(Status.OK))
			userOperationStatusUpdater.updateStatus(correlationId, UserStatus.REMOVAL_ACKNOWLEDGED, empty());
		else
			userOperationStatusUpdater.updateStatus(correlationId, UserStatus.REMOVAL_FAILED, getErrorMessage(ack.header.error));
	}

	@EventListener
	void receiveUserProjectRemovalResult(Payload<UserProjectRemovalResult> result) {
		CorrelationId correlationId = new CorrelationId(result.header.messageCorrelationId);
		if(result.header.status.equals(Status.OK))
			userOperationStatusUpdater.updateStatus(correlationId, UserStatus.REMOVED, empty());
		else
			userOperationStatusUpdater.updateStatus(correlationId, UserStatus.REMOVAL_FAILED, getErrorMessage(result.header.error));
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
