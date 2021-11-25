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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.ZonedDateTime;

@Component
class MessageSaverImpl implements MessageSaver {

	private final AgentPendingMessageSiteService agentPendingMessageSiteService;
	private final Clock clock;
	private final ObjectMapper objectMapper;

	MessageSaverImpl(AgentPendingMessageSiteService agentPendingMessageSiteService, Clock clock) {
		this.agentPendingMessageSiteService = agentPendingMessageSiteService;
		this.clock = clock;
		this.objectMapper = new FurmsPayloadConverter().mapper;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void save(String siteId, Payload<?> payload) {
		try {
			agentPendingMessageSiteService.create(SiteAgentPendingMessage.builder()
				.siteExternalId(new SiteExternalId(siteId))
				.correlationId(new CorrelationId(payload.header.messageCorrelationId))
				.jsonContent(objectMapper.writeValueAsString(payload))
				.utcSentAt(UTCTimeUtils.convertToUTCTime(ZonedDateTime.now(clock)))
				.build()
			);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
