/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client;

import io.imunity.furms.domain.authz.token.TokenRefreshResponse;
import io.imunity.furms.spi.tokens.AccessTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.LinkedHashMap;

@Component
public class UnityAccessTokenRepository implements AccessTokenRepository {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UnityClient unityClient;
	private final UnityOauthClient unityOauthClient;
	private final String revokeUri;
	private final String tokenUri;

	public UnityAccessTokenRepository(UnityClient unityClient,
			                          UnityOauthClient unityOauthClient,
			                          @Value("${spring.security.oauth2.client.provider.unity.revoke}") String revokeUri,
			                          @Value("${spring.security.oauth2.client.provider.unity.token-uri}") String tokenUri) {
		this.unityClient = unityClient;
		this.unityOauthClient = unityOauthClient;
		this.revokeUri = revokeUri;
		this.tokenUri = tokenUri;
	}

	@Override
	public void revoke(String accessToken, String clientId) {
		final URI uri = UriComponentsBuilder.fromUriString(revokeUri)
			.queryParam("token", accessToken)
			.queryParam("client_id", clientId)
			.queryParam("token_type_hint", "access_token")
			.queryParam("logout", "true")
			.build()
			.toUri();

		unityClient.post(uri, MediaType.APPLICATION_FORM_URLENCODED);
	}

	public TokenRefreshResponse refresh(String refreshToken, String clientId) throws Exception {
		try {
			final URI uri = new URI(tokenUri);
			final MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
			formData.add("grant_type", "refresh_token");
			formData.add("refresh_token", refreshToken);

			LinkedHashMap<String, Object> response = unityOauthClient.postForObject(uri, LinkedHashMap.class, formData, MediaType.APPLICATION_FORM_URLENCODED);

			return new TokenRefreshResponse(response);
		} catch (final Exception e) {
			LOG.error("Could not refresh token: ", e);
			throw e;
		}
	}

}
