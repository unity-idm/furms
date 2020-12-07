/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestOperations;

import java.util.HashMap;

public class FurmsOAuth2UserService extends DefaultOAuth2UserService {
	public FurmsOAuth2UserService(RestOperations restOperations) {
		super.setRestOperations(restOperations);
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		String key = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();
		OAuth2User oAuth2User = super.loadUser(userRequest);
		return new FurmsOAuth2User(oAuth2User, key, new HashMap<>());
	}
}
