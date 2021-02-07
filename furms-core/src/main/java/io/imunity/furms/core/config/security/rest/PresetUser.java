/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.core.config.security.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import io.imunity.furms.core.config.security.FurmsAuthenticatedUser;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

class PresetUser extends User implements FurmsAuthenticatedUser {

	private Map<ResourceId, Set<Role>> roles = new HashMap<>();

	PresetUser(String username, String password,
			Collection<? extends GrantedAuthority> authorities, 
			Map<ResourceId, Set<Role>> roles) {
		super(username, password, authorities);
		updateRoles(roles);
	}

	PresetUser(PresetUser user) {
		this(user.getUsername(), user.getPassword(), user.getAuthorities(), user.roles);
	}

	@Override
	public Map<ResourceId, Set<Role>> getRoles() {
		return roles;
	}

	@Override
	public void updateRoles(Map<ResourceId, Set<Role>> newRoles) {
		this.roles = new HashMap<>(newRoles.size());
		newRoles.entrySet().forEach(entry -> this.roles.put(entry.getKey(), Set.copyOf(entry.getValue())));
		this.roles = Map.copyOf(this.roles);
	}

	@Override
	public String getStringAttribute(String attributeName) {
		return null; //TODO
	}

	@Override
	public Map<String, Object> getAttributes() {
		return Collections.emptyMap(); //TODO
	}
}