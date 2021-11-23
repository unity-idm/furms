/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.sites.SiteExternalId;

public interface SiteAgentRetryService {
	void retry(SiteExternalId id, String json);
}
