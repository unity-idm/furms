/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.site_agent_pending_message;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.domain.site_agent_pending_messages.SiteAgentPendingMessage;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Set;

public interface SiteAgentConnectionService {
	Set<SiteAgentPendingMessage> findAll(SiteId siteId);
	boolean retry(SiteId siteId, CorrelationId correlationId);
	boolean delete(SiteId siteId, CorrelationId correlationId);
	PendingJob<SiteAgentStatus> getSiteAgentStatus(SiteId siteId);
}
