/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.sites.SiteExternalId;

public interface SiteAgentService {
	void initializeSiteConnection(SiteExternalId externalId);
	void removeSiteConnection(SiteExternalId siteShortId);
}
