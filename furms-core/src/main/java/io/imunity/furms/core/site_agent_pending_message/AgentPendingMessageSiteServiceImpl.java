/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.site_agent_pending_message;

import io.imunity.furms.core.config.security.method.FurmsPublicAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.site.api.AgentPendingMessageSiteService;
import io.imunity.furms.spi.site_agent_pending_message.SiteAgentPendingMessageRepository;
import io.imunity.furms.utils.UTCTimeUtils;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class AgentPendingMessageSiteServiceImpl implements AgentPendingMessageSiteService {

	private final SiteAgentPendingMessageRepository repository;
	private final Clock clock;

	AgentPendingMessageSiteServiceImpl(SiteAgentPendingMessageRepository repository, Clock clock) {
		this.repository = repository;
		this.clock = clock;
	}

	@Override
	@FurmsPublicAccess
	public Optional<SiteAgentPendingMessage> find(CorrelationId correlationId) {
		return repository.find(correlationId);
	}

	@Override
	@FurmsPublicAccess
	public void create(SiteAgentPendingMessage message) {
		repository.create(message);
	}

	@Override
	@FurmsPublicAccess
	public void setAsAcknowledged(CorrelationId id) {
		repository.updateAckTime(id, UTCTimeUtils.convertToUTCTime(ZonedDateTime.now(clock)));
	}

	@Override
	@FurmsPublicAccess
	public void delete(CorrelationId id) {
		repository.delete(id);
	}
}
