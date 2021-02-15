/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.authz;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.config.security.FurmsAuthenticatedUser;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.spi.roles.RoleLoader;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static io.imunity.furms.core.config.security.FurmsAuthenticatedUser.getCurrent;

@Service
public class AuthzServiceImpl implements AuthzService {
	private final RoleLoader roleLoader;

	public AuthzServiceImpl(RoleLoader roleLoader) {
		this.roleLoader = roleLoader;
	}

	@Override
	public Map<String, Object> getAttributes() {
		return getCurrent().getAttributes();
	}

	@Override
	public Map<ResourceId, Set<Role>> getRoles() {
		return getCurrent().getRoles();
	}

	@Override
	public void reloadRoles() {
		FurmsAuthenticatedUser authentication = getCurrent();
		String id = authentication.getStringAttribute("sub");
		authentication.updateRoles(roleLoader.loadUserRoles(id));
	}

	@Override
	public String getCurrentUserId(){
		return getCurrent().getStringAttribute("sub");
	}
}
