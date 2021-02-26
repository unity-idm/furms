/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.authz;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.Map;
import java.util.Set;

public interface AuthzService {
	FURMSUser getCurrentAuthNUser();
	void reloadRoles();
	Map<ResourceId, Set<Role>> getRoles();
	String getCurrentUserId();
	boolean isResourceMember(String resourceId, Role role);
}
