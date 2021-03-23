/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.oauth;

import io.imunity.furms.spi.tokens.AccessTokenRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
import static io.imunity.furms.domain.constant.RoutesConst.FRONT_LOGOUT_URL;
import static io.imunity.furms.domain.constant.RoutesConst.LOGOUT_URL;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@ConditionalOnBean(annotation = EnableWebSecurity.class)
public class FurmsOauthLogoutFilter extends OncePerRequestFilter {

	private final String VAADIN_REFRESH_COMMAND = "Vaadin-Refresh: ";

	private final OAuth2AuthorizedClientService auth2AuthorizedClientService;
	private final AccessTokenRepository accessTokenRepository;

	public FurmsOauthLogoutFilter(OAuth2AuthorizedClientService auth2AuthorizedClientService, AccessTokenRepository accessTokenRepository) {
		this.auth2AuthorizedClientService = auth2AuthorizedClientService;
		this.accessTokenRepository = accessTokenRepository;
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
					if (authorizedClient.getAccessToken() != null && authorizedClient.getAccessToken().getTokenValue() != null) {
						accessTokenRepository.revoke(authorizedClient.getAccessToken().getTokenValue(), authenticationToken.getAuthorizedClientRegistrationId());
					}
					new SecurityContextLogoutHandler().logout(request, response, authenticationToken);

					response.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
					response.getWriter().print(VAADIN_REFRESH_COMMAND + LOGOUT_URL);
					response.getWriter().flush();
					return;
				}
			}
		}
		filterChain.doFilter(request, response);
	}

	private boolean isFrontEndRequest(HttpServletRequest request) {
		return request != null
				&& request.getRequestURI().startsWith(FRONT)
				&& !request.getRequestURI().equals(FRONT_LOGOUT_URL);
	}

}
