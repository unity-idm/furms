/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.resource_access;

import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Set;

public interface ResourceAccessService {
	Set<String> findAddedUser(String projectId);
	Set<UserGrant> findUsersGrants(String projectId);
	Set<UserGrant> findUsersGrants(String projectId, FenixUserId fenixUserId);
	void grantAccess(GrantAccess grantAccess);
	void revokeAccess(GrantAccess grantAccess);
}
