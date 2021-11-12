/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.rabbitmq.site.models.Payload;
import io.imunity.furms.rabbitmq.site.models.converter.FurmsPayloadConverter;
import io.imunity.furms.site.api.SiteAgentPendingMessageResolver;
import io.imunity.furms.utils.UTCTimeUtils;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.ZonedDateTime;

import static io.imunity.furms.rabbitmq.site.client.QueueNamesService.getSiteId;

@Component
class FurmsRabbitTemplate extends RabbitTemplate {

	private final SiteAgentPendingMessageResolver repository;
	private final FurmsPayloadConverter converter;
	private final Clock clock;

	FurmsRabbitTemplate(SiteAgentPendingMessageResolver repository, Clock clock, ConnectionFactory connectionFactory) {
		super(connectionFactory);
		converter = new FurmsPayloadConverter();
		setMessageConverter(converter);
		this.repository = repository;
		this.clock = clock;
	}

	@Override
	public void convertAndSend(String routingKey, Object object){
		Payload<?> payload = (Payload<?>) object;
		try {
			repository.create(SiteAgentPendingMessage.builder()
				.siteExternalId(new SiteExternalId(getSiteId(routingKey)))
				.correlationId(new CorrelationId(payload.header.messageCorrelationId))
				.jsonContent(converter.mapper.writeValueAsString(object))
				.utcSentAt(UTCTimeUtils.convertToUTCTime(ZonedDateTime.now(clock)))
				.build()
			);
			super.convertAndSend(routingKey, object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
