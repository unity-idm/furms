/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user;

import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

public class FurmsUserContext extends DefaultOAuth2User {
	public final Map<FurmsRole, List<ResourceId>> roles;
	public ViewContext viewContext;

	public FurmsUserContext(OAuth2User defaultOAuth2User, String key, Map<FurmsRole, List<ResourceId>> roles) {
		super(defaultOAuth2User.getAuthorities(), defaultOAuth2User.getAttributes(), key);
		this.roles = roles;
	}
}
