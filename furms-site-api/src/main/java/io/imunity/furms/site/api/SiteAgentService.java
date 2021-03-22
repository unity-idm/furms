/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api;

import io.imunity.furms.domain.site_agent.SiteAgentStatus;

import java.util.concurrent.CompletableFuture;

public interface SiteAgentService {
	void initializeSiteConnection(String siteId);
	void removeSiteConnection(String siteId);
	CompletableFuture<SiteAgentStatus> getStatus(String siteId);
}
