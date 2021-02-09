/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

import java.util.Map;
import java.util.Set;

import org.springframework.security.core.context.SecurityContextHolder;

public interface FurmsAuthenticatedUser {

	Map<ResourceId, Set<Role>> getRoles();
	void updateRoles(Map<ResourceId, Set<Role>> newRoles);

	String getStringAttribute(String attributeName);
	//TODO switch to better typing of attr values. 
	//FIXME required attributes must be exposed with separate methods
	Map<String, Object> getAttributes();

	static FurmsAuthenticatedUser getCurrent() {
		return (FurmsAuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
}
