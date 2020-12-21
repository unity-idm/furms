/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import static io.imunity.furms.domain.constant.LoginFlowConst.LOGOUT_SUCCESS_URL;
import static org.springframework.http.RequestEntity.post;

@Component
class TokenRevoker implements LogoutSuccessHandler {
	private final RestTemplate unityRestTemplate;
	private final OAuth2AuthorizedClientService auth2AuthorizedClientService;
	@Value("${spring.security.oauth2.client.provider.unity.revoke}")
	private String uri;

	TokenRevoker(RestTemplate unityRestTemplate, OAuth2AuthorizedClientService auth2AuthorizedClientService) {
		this.unityRestTemplate = unityRestTemplate;
		this.auth2AuthorizedClientService = auth2AuthorizedClientService;
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
		OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
		DefaultOAuth2User principal = (DefaultOAuth2User)oAuth2AuthenticationToken.getPrincipal();
		String authorizedClientRegistrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
		OAuth2AuthorizedClient oAuth2AuthorizedClient = auth2AuthorizedClientService.loadAuthorizedClient(authorizedClientRegistrationId, principal.getName());
		String accessToken = oAuth2AuthorizedClient.getAccessToken().getTokenValue();
		String clientId = oAuth2AuthorizedClient.getClientRegistration().getClientId();

		revokeToken(accessToken, clientId);
		httpServletResponse.sendRedirect(LOGOUT_SUCCESS_URL);
	}

	private void revokeToken(String accessToken, String clientId) {
		URI uriWithParams = UriComponentsBuilder.fromUriString(uri)
			.queryParam("token", accessToken)
			.queryParam("client_id", clientId)
			.queryParam("token_type_hint", "access_token")
			.queryParam("logout", "true")
			.build()
			.toUri();

		unityRestTemplate.exchange(post(uriWithParams).build(), Void.class);
	}
}
