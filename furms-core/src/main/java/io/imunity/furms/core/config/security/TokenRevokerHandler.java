/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security;

import io.imunity.furms.core.config.security.oauth.FurmsOAuthAuthenticatedUser;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.authn.UserLoggedOutEvent;
import io.imunity.furms.spi.tokens.AccessTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static io.imunity.furms.domain.constant.RoutesConst.POST_LOGOUT_PAGE_URL;

@Component
class TokenRevokerHandler implements LogoutSuccessHandler {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final AccessTokenRepository accessTokenRepository;
	private final OAuth2AuthorizedClientService auth2AuthorizedClientService;
	private final ApplicationEventPublisher publisher;

	TokenRevokerHandler(AccessTokenRepository accessTokenRepository, 
			OAuth2AuthorizedClientService auth2AuthorizedClientService,
			ApplicationEventPublisher publisher) {
		this.accessTokenRepository = accessTokenRepository;
		this.auth2AuthorizedClientService = auth2AuthorizedClientService;
		this.publisher = publisher;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, 
			Authentication authentication) throws IOException, ServletException {

		OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
		revokeOauthToken(oAuth2AuthenticationToken);
		httpServletResponse.sendRedirect(POST_LOGOUT_PAGE_URL);
	}

	private void revokeOauthToken(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
		if (oAuth2AuthenticationToken == null) //may be null if request to logout is made while session expired on its own
			return;
		LOG.info("Closing Unity authn session & invalidating oauth token");
		DefaultOAuth2User principal = (DefaultOAuth2User)oAuth2AuthenticationToken.getPrincipal();
		String authorizedClientRegistrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
		OAuth2AuthorizedClient oAuth2AuthorizedClient = auth2AuthorizedClientService.loadAuthorizedClient(
				authorizedClientRegistrationId, principal.getName());
		String accessToken = oAuth2AuthorizedClient.getAccessToken().getTokenValue();
		String clientId = oAuth2AuthorizedClient.getClientRegistration().getClientId();

		accessTokenRepository.revoke(accessToken, clientId);

		try {
			FURMSUser furmsUser = ((FurmsOAuthAuthenticatedUser)oAuth2AuthenticationToken.getPrincipal()).furmsUser;
			publisher.publishEvent(new UserLoggedOutEvent(furmsUser));
		} catch (Exception e){
			LOG.error("Publishing user logged out event failed", e);
		}
	}
}
