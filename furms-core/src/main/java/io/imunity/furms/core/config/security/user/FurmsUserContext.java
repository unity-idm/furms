/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user;

import io.imunity.furms.domain.roles.ResourceId;
import io.imunity.furms.domain.roles.Role;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Set;

public class FurmsUserContext extends DefaultOAuth2User {
	public final Map<ResourceId, Set<Role>> roles;

	public FurmsUserContext(OAuth2User defaultOAuth2User, String key, Map<ResourceId, Set<Role>> roles) {
		super(defaultOAuth2User.getAuthorities(), defaultOAuth2User.getAttributes(), key);
		this.roles = roles;
	}
}
