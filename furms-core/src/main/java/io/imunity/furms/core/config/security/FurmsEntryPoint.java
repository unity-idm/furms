/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static io.imunity.furms.domain.constant.LoginFlowConst.PROXY_AUTH_PARAM;

class FurmsEntryPoint extends LoginUrlAuthenticationEntryPoint {
	private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	FurmsEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
	                     AuthenticationException authException) throws IOException{
		String redirectUrl = super.buildRedirectUrlToLoginPage(request, response, authException);
		if(request.getParameter(PROXY_AUTH_PARAM) != null)
			redirectUrl += "?" + PROXY_AUTH_PARAM;
		redirectStrategy.sendRedirect(request, response, redirectUrl);
	}
}
