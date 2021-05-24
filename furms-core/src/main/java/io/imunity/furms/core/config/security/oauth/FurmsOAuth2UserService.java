/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.oauth;


import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.roles.RoleLoader;
import io.imunity.furms.spi.roles.RoleLoadingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestOperations;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Set;

public class FurmsOAuth2UserService extends DefaultOAuth2UserService {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final RoleLoader roleLoader;

	public FurmsOAuth2UserService(RestOperations restOperations, RoleLoader roleLoader) {
		super.setRestOperations(restOperations);
		this.roleLoader = roleLoader;
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		LOG.debug("Will get OAuth user from Unity");
		OAuth2User oAuth2User = super.loadUser(userRequest);

		String key = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint()
			.getUserNameAttributeName();
		String sub = oAuth2User.getAttribute("sub");
		try {
			Map<ResourceId, Set<Role>> roles = roleLoader.loadUserRoles(new PersistentId(sub));
			return new FurmsOAuthAuthenticatedUser(oAuth2User, key, roles);
		}catch (RoleLoadingException e){
			throw new OAuth2AuthenticationException(new OAuth2Error(e.code), e);
		}
	}
}
