/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_access;

import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UsersWithProjectAccess;
import io.imunity.furms.domain.resource_access.UserGrant;

import java.util.Set;

public interface ResourceAccessService {
	Set<String> findAddedUser(String projectId);
	Set<UsersWithProjectAccess> findAddedUserBySiteId(String siteId);
	Set<UserGrant> findUsersGrants(String projectId);
	Set<UserGrant> findCurrentUserGrants(String projectId);
	void grantAccess(GrantAccess grantAccess);
	void revokeAccess(GrantAccess grantAccess);
}
