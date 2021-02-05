/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.authz;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.core.config.security.user.FurmsAuthenticatedUser;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.spi.roles.RoleLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class AuthzServiceImpl implements AuthzService {
	private final RoleLoader roleLoader;

	public AuthzServiceImpl(RoleLoader roleLoader) {
		this.roleLoader = roleLoader;
	}

	@Override
	public Map<String, Object> getAttributes() {
		FurmsAuthenticatedUser authentication = (FurmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return authentication.getAttributes();
	}

	@Override
	public Map<ResourceId, Set<Role>> getRoles() {
		FurmsAuthenticatedUser authentication = (FurmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return authentication.roles;
	}

	@Override
	public void reloadRoles() {
		FurmsAuthenticatedUser authentication = (FurmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String id = authentication.getAttribute("sub");
		authentication.roles.clear();
		authentication.roles.putAll(roleLoader.loadUserRoles(id));
	}

	@Override
	public String getCurrentUserId(){
		FurmsUser authentication = (FurmsUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return authentication.getAttribute("sub");
	}
}
