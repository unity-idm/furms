/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.SiteAgentException;
import io.imunity.furms.rabbitmq.site.models.*;
import io.imunity.furms.site.api.message_resolver.UserAllocationMessageResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Service
class SiteAgentResourceAccessServiceImpl implements SiteAgentResourceAccessService {
	private final RabbitTemplate rabbitTemplate;
	private final UserAllocationMessageResolver messageResolver;

	SiteAgentResourceAccessServiceImpl(RabbitTemplate rabbitTemplate, UserAllocationMessageResolver messageResolver) {
		this.rabbitTemplate = rabbitTemplate;
		this.messageResolver = messageResolver;
	}

	@EventListener
	void receiveUserAllocationBlockAccessRequestAck(Payload<UserAllocationBlockAccessRequestAck> ack) {
		AccessStatus status = ack.header.status.equals(Status.FAILED) ? AccessStatus.REVOKE_FAILED : AccessStatus.REVOKE_ACKNOWLEDGED;
		CorrelationId correlationId = new CorrelationId(ack.header.messageCorrelationId);
		String msg = Optional.ofNullable(ack.header.error).map(e -> e.message).orElse(null);
		messageResolver.update(correlationId, status, msg);
	}

	@EventListener
	void receiveUserAllocationBlockAccessResult(Payload<UserAllocationBlockAccessResult> result) {
		AccessStatus status = result.header.status.equals(Status.FAILED) ? AccessStatus.REVOKE_FAILED : AccessStatus.REVOKED;
		CorrelationId correlationId = new CorrelationId(result.header.messageCorrelationId);
		String msg = Optional.ofNullable(result.header.error).map(e -> e.message).orElse(null);
		messageResolver.update(correlationId, status, msg);
	}

	@EventListener
	void receiveUserAllocationGrantAccessRequestAck(Payload<UserAllocationGrantAccessRequestAck> ack) {
		AccessStatus status = ack.header.status.equals(Status.FAILED) ? AccessStatus.GRANT_FAILED : AccessStatus.GRANT_ACKNOWLEDGED;
		CorrelationId correlationId = new CorrelationId(ack.header.messageCorrelationId);
		String msg = Optional.ofNullable(ack.header.error).map(e -> e.message).orElse(null);
		messageResolver.update(correlationId, status, msg);
	}

	@EventListener
	void receiveUserAllocationGrantAccessResult(Payload<UserAllocationGrantAccessResult> result) {
		AccessStatus status = result.header.status.equals(Status.FAILED) ? AccessStatus.GRANT_FAILED : AccessStatus.GRANTED;
		CorrelationId correlationId = new CorrelationId(result.header.messageCorrelationId);
		String msg = Optional.ofNullable(result.header.error).map(e -> e.message).orElse(null);
		messageResolver.update(correlationId, status, msg);
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public void grantAccess(CorrelationId correlationId, GrantAccess grantAccess) {
		UserAllocationGrantAccessRequest userAllocationGrantAccessRequest = new UserAllocationGrantAccessRequest(grantAccess.allocationId, grantAccess.fenixUserId, grantAccess.projectId);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(grantAccess.siteId.externalId), new Payload<>(new Header(VERSION, correlationId.id), userAllocationGrantAccessRequest));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}

	@Override
	@Transactional(propagation = Propagation.NESTED)
	public void revokeAccess(CorrelationId correlationId, GrantAccess grantAccess) {
		UserAllocationBlockAccessRequest userAllocationBlockAccessRequest = new UserAllocationBlockAccessRequest(grantAccess.allocationId, grantAccess.fenixUserId, grantAccess.projectId);
		try {
			rabbitTemplate.convertAndSend(getFurmsPublishQueueName(grantAccess.siteId.externalId), new Payload<>(new Header(VERSION, correlationId.id), userAllocationBlockAccessRequest));
		}catch (AmqpConnectException e){
			throw new SiteAgentException("Queue is unavailable", e);
		}
	}
}
