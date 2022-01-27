/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.mvc;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_SUCCESS_URL;
import static io.imunity.furms.domain.constant.RoutesConst.OAUTH_START_URL;
import static io.imunity.furms.domain.constant.RoutesConst.OAUTH_START_WITH_AUTOPROXY_URL;
import static io.imunity.furms.domain.constant.RoutesConst.REGISTRATION_ID;

@Service
class RedirectService {

	public String getRedirectURL(boolean showSignInOptions) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication.isAuthenticated() && isNotAnonymousUser(authentication))
			return LOGIN_SUCCESS_URL;

		String forwardURL = OAUTH_START_WITH_AUTOPROXY_URL;
		if (showSignInOptions)
			forwardURL = OAUTH_START_URL;
		return forwardURL + REGISTRATION_ID;
	}

	private boolean isNotAnonymousUser(Authentication authentication) {
		return authentication.getAuthorities().stream()
			.map(GrantedAuthority::getAuthority)
			.noneMatch(authority -> authority.equals("ROLE_ANONYMOUS"));
	}
}
