/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api;

import io.imunity.furms.domain.sites.SiteExternalId;

import java.util.Set;

public interface SiteExternalIdsResolver {
	Set<SiteExternalId> findAllIds();
}
