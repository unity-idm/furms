/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;

import java.util.Optional;

public interface SiteAgentPendingMessageResolver {
	Optional<SiteAgentPendingMessage> find(CorrelationId correlationId);

	void create(SiteAgentPendingMessage message);

	void updateToAck(CorrelationId id);

	void delete(CorrelationId id);
}
