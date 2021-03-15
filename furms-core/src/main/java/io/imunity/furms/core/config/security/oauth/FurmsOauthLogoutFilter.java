/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.oauth;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.Instant;

import static io.imunity.furms.domain.constant.RoutesConst.FRONT;

@Component
public class FurmsOauthLogoutFilter extends OncePerRequestFilter {

	private final OAuth2AuthorizedClientService auth2AuthorizedClientService;

	public FurmsOauthLogoutFilter(OAuth2AuthorizedClientService auth2AuthorizedClientService) {
		this.auth2AuthorizedClientService = auth2AuthorizedClientService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
		if (isFrontEndRequest(request)) {
			final SecurityContext context = SecurityContextHolder.getContext();
			if (context != null && context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
				final OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) context.getAuthentication();
				final DefaultOAuth2User principal = (DefaultOAuth2User) authenticationToken.getPrincipal();
				final OAuth2AuthorizedClient authorizedClient = auth2AuthorizedClientService.loadAuthorizedClient(authenticationToken.getAuthorizedClientRegistrationId(), principal.getName());

				if (authorizedClient.getAccessToken() == null
						|| authorizedClient.getAccessToken().getExpiresAt() == null
						|| authorizedClient.getAccessToken().getExpiresAt().isBefore(Instant.now())) {
					new SecurityContextLogoutHandler().logout(request, response, authenticationToken);
				}
			}
		}
		filterChain.doFilter(request, response);
	}

	private boolean isFrontEndRequest(HttpServletRequest request) {
		return request != null
				&& request.getRequestURI().startsWith(FRONT);
	}

}
