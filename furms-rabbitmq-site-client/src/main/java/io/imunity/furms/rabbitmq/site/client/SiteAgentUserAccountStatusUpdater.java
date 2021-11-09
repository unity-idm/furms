/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.domain.users.SiteAgentSetUserAccountStatusRequest;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusReason;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusRequest;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusRequestAck;
import io.imunity.furms.rabbitmq.site.models.SetUserStatusResult;
import io.imunity.furms.rabbitmq.site.models.UserAccountStatus;
import io.imunity.furms.site.api.status_updater.SiteAgentUserStatusFlowUpdater;
import io.imunity.furms.site.api.status_updater.UserAccountStatusUpdater;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Service
class SiteAgentUserAccountStatusUpdater implements UserAccountStatusUpdater {
	private final RabbitTemplate rabbitTemplate;
	private final SiteAgentUserStatusFlowUpdater receiveHandler;

	SiteAgentUserAccountStatusUpdater(RabbitTemplate rabbitTemplate, SiteAgentUserStatusFlowUpdater receiveHandler) {
		this.rabbitTemplate = rabbitTemplate;
		this.receiveHandler = receiveHandler;
	}

	@EventListener
	void receiveSetUserStatusRequestAck(Payload<SetUserStatusRequestAck> ack) {
		receiveHandler.updateSetUserStatusRequestAck(new CorrelationId(ack.header.messageCorrelationId));
	}

	@EventListener
	void receiveSetUserStatusResult(Payload<SetUserStatusResult> result) {
		receiveHandler.updateSetUserStatusResult(new CorrelationId(result.header.messageCorrelationId));
	}

	@Override
	public void setStatus(SiteAgentSetUserAccountStatusRequest userStatus) {
		final SetUserStatusRequest request = new SetUserStatusRequest(
				userStatus.fenixUserId.id,
				UserAccountStatus.valueOf(userStatus.status.name()),
				SetUserStatusReason.valueOf(userStatus.reason.name()));
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(userStatus.siteExternalId),
					new Payload<>(new Header(VERSION, userStatus.correlationId.id), request));
		} catch (AmqpConnectException e) {
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
