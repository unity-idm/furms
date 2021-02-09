/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.oauth;

import io.imunity.furms.core.config.security.FurmsAuthenticatedUser;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;

import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class FurmsOAuthAuthenticatedUser extends DefaultOAuth2User implements FurmsAuthenticatedUser {
	public Map<ResourceId, Set<Role>> roles;

	public FurmsOAuthAuthenticatedUser(OAuth2User defaultOAuth2User, String key, Map<ResourceId, Set<Role>> roles) {
		super(defaultOAuth2User.getAuthorities(), defaultOAuth2User.getAttributes(), key);
		updateRoles(roles);
	}

	@Override
	public Map<ResourceId, Set<Role>> getRoles() {
		return roles;
	}

	@Override
	public void updateRoles(Map<ResourceId, Set<Role>> newRoles) {
		this.roles = new HashMap<>(newRoles.size());
		newRoles.entrySet().forEach(entry -> roles.put(entry.getKey(), Set.copyOf(entry.getValue())));
	}

	@Override
	public String getStringAttribute(String attributeName) {
		return super.getAttribute(attributeName);
	}
}
