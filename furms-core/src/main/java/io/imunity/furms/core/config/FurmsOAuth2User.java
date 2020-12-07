/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config;

import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

public class FurmsOAuth2User extends DefaultOAuth2User {
	private final Map<String,String> roles;

	public FurmsOAuth2User(OAuth2User defaultOAuth2User, String key, Map<String, String> roles) {
		super(defaultOAuth2User.getAuthorities(), defaultOAuth2User.getAttributes(), key);
		this.roles = roles;
	}

	public Map<String, String> getRoles() {
		return roles;
	}
}
