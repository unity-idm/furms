/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.oauth;

import com.google.common.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static io.imunity.furms.domain.constant.RoutesConst.FRONT;

@Component
public class FurmsOauthTokenExtender extends OncePerRequestFilter {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final static String HEARTBEAT_PARAM_KEY = "v-r";
	private final static String HEARTBEAT_PARAM_VALUE = "heartbeat";

	private final OAuth2AuthorizedClientService auth2AuthorizedClientService;
	private final TokenRefreshHandler tokenRefreshHandler;
	private final Cache<String, String> oauthTokenCache;

	FurmsOauthTokenExtender(OAuth2AuthorizedClientService auth2AuthorizedClientService,
	                        TokenRefreshHandler tokenRefreshHandler,
	                        Cache<String, String> oauthTokenCache) {
		this.auth2AuthorizedClientService = auth2AuthorizedClientService;
		this.tokenRefreshHandler = tokenRefreshHandler;
		this.oauthTokenCache = oauthTokenCache;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		if (requestIsAbleToRefreshSession(request)) {
			final SecurityContext context = SecurityContextHolder.getContext();
			if (context != null && context.getAuthentication() != null && context.getAuthentication().isAuthenticated()) {
				final OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) context.getAuthentication();
				final DefaultOAuth2User principal = (DefaultOAuth2User)oAuth2AuthenticationToken.getPrincipal();
				final OAuth2AuthorizedClient oAuth2AuthorizedClient = auth2AuthorizedClientService.loadAuthorizedClient(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId(), principal.getName());

				if (oAuth2AuthorizedClient.getAccessToken() != null && oauthTokenCache.getIfPresent(oAuth2AuthorizedClient.getAccessToken().getTokenValue()) == null) {
					try {
						final OAuth2AccessToken newToken = tokenRefreshHandler.refresh(oAuth2AuthorizedClient, oAuth2AuthenticationToken, principal);
						oauthTokenCache.put(newToken.getTokenValue(), principal.getName());
					} catch (Exception e) {
						LOG.error("Could not refresh Oauth token: ", e);
						return;
					}
				}
			}
		}
		filterChain.doFilter(request, response);
	}

	private boolean requestIsAbleToRefreshSession(HttpServletRequest request) {
		return request != null
				&& request.getRequestURI().startsWith(FRONT)
				&& isNotHeartBeat(request);
	}

	private boolean isNotHeartBeat(HttpServletRequest request) {
		return request.getParameterMap() != null
				&& !HEARTBEAT_PARAM_VALUE.equals(request.getParameter(HEARTBEAT_PARAM_KEY));
	}
}
