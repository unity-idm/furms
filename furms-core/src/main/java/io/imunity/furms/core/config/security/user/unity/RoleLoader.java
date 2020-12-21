/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user.unity;

import io.imunity.furms.core.config.security.user.resource.ResourceId;
import io.imunity.furms.core.config.security.user.role.Role;

import java.util.Map;
import java.util.Set;

public interface RoleLoader {
	Map<ResourceId, Set<Role>> loadUserRoles(String id);
}
