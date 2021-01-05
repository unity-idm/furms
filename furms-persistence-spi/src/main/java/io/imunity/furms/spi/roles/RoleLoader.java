/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.roles;


import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

import java.util.Map;
import java.util.Set;

public interface RoleLoader {
	Map<ResourceId, Set<Role>> loadUserRoles(String id);
}
