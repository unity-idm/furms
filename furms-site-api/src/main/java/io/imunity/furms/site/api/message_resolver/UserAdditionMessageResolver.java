/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;

public interface UserAdditionMessageResolver {
	boolean isMessageCorrelated(CorrelationId id, SiteExternalId siteExternalId);
}
