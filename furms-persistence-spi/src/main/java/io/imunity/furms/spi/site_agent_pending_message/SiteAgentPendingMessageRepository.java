/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.site_agent_pending_message;


import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.SiteExternalId;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface SiteAgentPendingMessageRepository {
	Set<SiteAgentPendingMessage> findAll(SiteExternalId siteId);

	Optional<SiteAgentPendingMessage> find(CorrelationId correlationId);

	void create(SiteAgentPendingMessage message);

	void overwriteSentTime(CorrelationId id, LocalDateTime sentAt);

	void updateAckTime(CorrelationId id, LocalDateTime ackAt);

	void delete(CorrelationId id);
}
