/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.oauth;

import io.imunity.furms.spi.tokens.AccessTokenRepository;
import io.imunity.furms.unity.client.UnityClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.invoke.MethodHandles;
import java.net.URI;

@Component
class UnityOauthAccessTokenRepository implements AccessTokenRepository {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UnityClient unityClient;
	private final UnityOauthProperties unityOauthProperties;

	UnityOauthAccessTokenRepository(UnityClient unityClient,
	                                       UnityOauthProperties unityOauthProperties) {
		this.unityClient = unityClient;
		this.unityOauthProperties = unityOauthProperties;
	}

	@Override
	public void revoke(String accessToken, String clientId) {
		final URI uri = UriComponentsBuilder.fromUriString(unityOauthProperties.getRevoke())
			.queryParam("token", accessToken)
			.queryParam("client_id", clientId)
			.queryParam("token_type_hint", "access_token")
			.queryParam("logout", "true")
			.build()
			.toUri();

		unityClient.post(uri, MediaType.APPLICATION_FORM_URLENCODED);
	}
}
