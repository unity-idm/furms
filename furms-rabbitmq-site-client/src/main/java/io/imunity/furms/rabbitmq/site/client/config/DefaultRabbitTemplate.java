/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.converter.FurmsPayloadConverter;
import io.imunity.furms.site.api.AgentPendingMessageSiteService;
import io.imunity.furms.utils.UTCTimeUtils;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getSiteId;

@Primary
@Component
class DefaultRabbitTemplate extends RabbitTemplate {

	private final AgentPendingMessageSiteService agentPendingMessageSiteService;
	private final ObjectMapper objectMapper;
	private final Clock clock;

	DefaultRabbitTemplate(AgentPendingMessageSiteService agentPendingMessageSiteService, Clock clock, ConnectionFactory connectionFactory, MessageConverter converter) {
		super(connectionFactory);
		setMessageConverter(converter);
		this.objectMapper = new FurmsPayloadConverter().mapper;
		this.agentPendingMessageSiteService = agentPendingMessageSiteService;
		this.clock = clock;
	}

	@Override
	public void convertAndSend(String routingKey, Object object){
		Payload<?> payload = (Payload<?>) object;
		try {
			agentPendingMessageSiteService.create(SiteAgentPendingMessage.builder()
				.siteExternalId(new SiteExternalId(getSiteId(routingKey)))
				.correlationId(new CorrelationId(payload.header.messageCorrelationId))
				.jsonContent(objectMapper.writeValueAsString(object))
				.utcSentAt(UTCTimeUtils.convertToUTCTime(ZonedDateTime.now(clock)))
				.build()
			);
			super.convertAndSend(routingKey, object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
