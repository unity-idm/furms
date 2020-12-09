/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.user;

import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

public class FurmsOAuth2User extends DefaultOAuth2User {
	public final Map<String, List<Attribute>> attributes;
	public final Map<String, List<Role>> roles;
	public String currentGroup;

	public FurmsOAuth2User(OAuth2User defaultOAuth2User, String key, Map<String, List<Attribute>> attributes,
	                       Map<String, List<Role>> roles) {
		super(defaultOAuth2User.getAuthorities(), defaultOAuth2User.getAttributes(), key);
		this.attributes = attributes;
		this.roles = roles;
	}
}
