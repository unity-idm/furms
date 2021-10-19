/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.user_site_access;

import io.imunity.furms.domain.user_site_access.UsersSitesAccesses;
import io.imunity.furms.domain.users.FenixUserId;

public interface UserSiteAccessService {
	void addAccess(String siteId, String projectId, FenixUserId userId);
	void removeAccess(String siteId, String projectId, FenixUserId userId);
	UsersSitesAccesses getUsersSitesAccesses(String projectId);
}
