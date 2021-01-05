/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity;

import io.imunity.furms.spi.tokens.AccessTokenRevoker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class UnityAccessTokenRevoker implements AccessTokenRevoker {
	private final UnityClient unityClient;

	@Value("${spring.security.oauth2.client.provider.unity.revoke}")
	private String uri;

	public UnityAccessTokenRevoker(UnityClient unityClient) {
		this.unityClient = unityClient;
	}

	@Override
	public void revoke(String accessToken, String clientId) {
		URI uri = UriComponentsBuilder.fromUriString(this.uri)
			.queryParam("token", accessToken)
			.queryParam("client_id", clientId)
			.queryParam("token_type_hint", "access_token")
			.queryParam("logout", "true")
			.build()
			.toUri();

		unityClient.post(uri, MediaType.APPLICATION_FORM_URLENCODED);
	}
}
