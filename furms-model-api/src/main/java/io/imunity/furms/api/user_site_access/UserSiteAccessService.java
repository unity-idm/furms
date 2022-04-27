/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.user_site_access;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_site_access.UsersSitesAccesses;
import io.imunity.furms.domain.users.FenixUserId;

public interface UserSiteAccessService {
	void addAccess(SiteId siteId, ProjectId projectId, FenixUserId userId);
	void removeAccess(SiteId siteId, ProjectId projectId, FenixUserId userId);
	UsersSitesAccesses getUsersSitesAccesses(ProjectId projectId);
}
