/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security;

import io.imunity.furms.core.config.security.oauth.FurmsOAuthAuthenticatedUser;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.authn.UserLoggedInEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.imunity.furms.domain.constant.RoutesConst.LOGIN_SUCCESS_URL;

@Component
class FurmsAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private final ApplicationEventPublisher publisher;

	FurmsAuthenticationSuccessHandler(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
		setDefaultTargetUrl(LOGIN_SUCCESS_URL);
		setAlwaysUseDefaultTargetUrl(false);
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
		super.onAuthenticationSuccess(request, response, authentication);
		FURMSUser furmsUser = ((FurmsOAuthAuthenticatedUser)authentication.getPrincipal()).furmsUser;
		publisher.publishEvent(new UserLoggedInEvent(furmsUser));
	}
}
