/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.oauth;

import io.imunity.furms.domain.authz.token.TokenRefreshResponse;
import io.imunity.furms.spi.tokens.AccessTokenRepository;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Component;

import static org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER;

@Component
public class TokenRefreshHandler {

	private final OAuth2AuthorizedClientService auth2AuthorizedClientService;
	private final AccessTokenRepository tokenRepository;

	TokenRefreshHandler(OAuth2AuthorizedClientService auth2AuthorizedClientService, AccessTokenRepository tokenRepository) {
		this.auth2AuthorizedClientService = auth2AuthorizedClientService;
		this.tokenRepository = tokenRepository;
	}

	OAuth2AccessToken refresh(OAuth2AuthorizedClient authorizedClient, OAuth2AuthenticationToken authenticationToken, DefaultOAuth2User principal) throws Exception {

		final TokenRefreshResponse response = tokenRepository.refresh(
				authorizedClient.getRefreshToken().getTokenValue(),
				authenticationToken.getAuthorizedClientRegistrationId());
		final OAuth2AccessToken refreshedToken = new OAuth2AccessToken(BEARER,
				response.getTokenValue(),
				response.getIssuedAt(),
				response.getExpiresAt(),
				response.getScopes());
		final OAuth2AuthorizedClient refreshedAuthorizedClient = new OAuth2AuthorizedClient(
				authorizedClient.getClientRegistration(),
				principal.getName(),
				refreshedToken,
				authorizedClient.getRefreshToken());

		auth2AuthorizedClientService.removeAuthorizedClient(authenticationToken.getAuthorizedClientRegistrationId(), principal.getName());

		auth2AuthorizedClientService.saveAuthorizedClient(refreshedAuthorizedClient, authenticationToken);

		return refreshedToken;
	}

}
