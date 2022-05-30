/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.user_site_access;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Map;
import java.util.Set;

public interface UserSiteAccessRepository {
	Set<ProjectId> findAllUserProjectIds(SiteId siteId, FenixUserId userId);
	Map<SiteId, Set<FenixUserId>> findAllUserGroupedBySiteId(ProjectId projectId);
	void add(SiteId siteId, ProjectId projectId, FenixUserId userId);
	void remove(SiteId siteId, ProjectId projectId, FenixUserId userId);
	void remove(ProjectId projectId, FenixUserId userId);
	boolean exists(SiteId siteId, ProjectId projectId, FenixUserId userId);
	void deleteAll();
}
