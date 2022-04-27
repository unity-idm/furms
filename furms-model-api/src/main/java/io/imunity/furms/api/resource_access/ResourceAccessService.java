/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_access;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UsersWithProjectAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Set;

public interface ResourceAccessService {
	Set<String> findAddedUser(ProjectId projectId);
	Set<UsersWithProjectAccess> findAddedUserBySiteId(SiteId siteId);
	Set<UserGrant> findUsersGrants(ProjectId projectId);
	Set<UserGrant> findCurrentUserGrants(ProjectId projectId);
	void grantAccess(GrantAccess grantAccess);
	void revokeAccess(GrantAccess grantAccess);
}
