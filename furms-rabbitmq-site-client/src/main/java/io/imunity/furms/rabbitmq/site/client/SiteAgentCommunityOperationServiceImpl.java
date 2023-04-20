/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.communities.CommunityInstallation;
import io.imunity.furms.domain.communities.CommunityUpdate;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityInstallationRequest;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityInstallationRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityRemovalRequestAck;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityUpdateRequest;
import io.imunity.furms.rabbitmq.site.models.AgentCommunityUpdateRequestAck;
import io.imunity.furms.rabbitmq.site.models.Header;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.site.api.SiteExternalIdsResolver;
import io.imunity.furms.site.api.site_agent.SiteAgentCommunityOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getFurmsPublishQueueName;
import static io.imunity.furms.rabbitmq.site.models.consts.Protocol.VERSION;

@Service
class SiteAgentCommunityOperationServiceImpl implements SiteAgentCommunityOperationService
{
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final RabbitTemplate rabbitTemplate;
	private final SiteExternalIdsResolver siteExternalIdsResolver;

	SiteAgentCommunityOperationServiceImpl(RabbitTemplate rabbitTemplate, SiteExternalIdsResolver siteExternalIdsResolver) {
		this.rabbitTemplate = rabbitTemplate;
		this.siteExternalIdsResolver = siteExternalIdsResolver;
	}

	@EventListener
	void receiveAgentCommunityInstallationAck(Payload<AgentCommunityInstallationRequestAck> ack) {
		//do nothing
	}

	@EventListener
	void receiveAgentCommunityUpdateAck(Payload<AgentCommunityUpdateRequestAck> ack) {
		//do nothing
	}

	@EventListener
	void receiveAgentCommunityRemoveAck(Payload<AgentCommunityRemovalRequestAck> ack) {
		//do nothing
	}

	@Override
	public void installCommunity(CommunityInstallation community)
	{
		AgentCommunityInstallationRequest request = new AgentCommunityInstallationRequest(
			community.id.id.toString(),
			community.name,
			community.description
		);
		siteExternalIdsResolver.findAllIds().forEach(externalId -> {
			try {
				rabbitTemplate.convertAndSend(
					getFurmsPublishQueueName(externalId),
					new Payload<>(new Header(VERSION, CorrelationId.randomID().id), request)
				);
			}catch (AmqpConnectException e){
				LOG.error("Queue is unavailable", e);
			}
		});
	}

	@Override
	public void updateCommunity(CommunityUpdate community)
	{
		AgentCommunityUpdateRequest request = new AgentCommunityUpdateRequest(
			community.id.id.toString(),
			community.name,
			community.description
		);
		siteExternalIdsResolver.findAllIds().forEach(externalId -> {
			try {
				rabbitTemplate.convertAndSend(
					getFurmsPublishQueueName(externalId),
					new Payload<>(new Header(VERSION, CorrelationId.randomID().id), request)
				);
			}catch (AmqpConnectException e){
				LOG.error("Queue is unavailable", e);
			}
		});
	}

	@Override
	public void removeCommunity(CommunityId id)
	{
		AgentCommunityRemovalRequest request = new AgentCommunityRemovalRequest(
			id.id.toString()
		);
		siteExternalIdsResolver.findAllIds().forEach(externalId -> {
			try {
				rabbitTemplate.convertAndSend(
					getFurmsPublishQueueName(externalId),
					new Payload<>(new Header(VERSION, CorrelationId.randomID().id), request)
				);
			}catch (AmqpConnectException e){
				LOG.error("Queue is unavailable", e);
			}
		});
	}
}
