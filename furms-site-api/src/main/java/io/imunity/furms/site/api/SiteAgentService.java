/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api;

import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;

public interface SiteAgentService {
	void initializeSiteConnection(String siteShortId);
	void removeSiteConnection(String siteShortId);
	PendingJob<SiteAgentStatus> getStatus(String siteShortId);
}
