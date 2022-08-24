/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.oauth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;

import static io.imunity.furms.unity.config.WebClientConfig.OAUTH_UNITY_CLIENT;

@Component
class UnityOauthClient {

	private final WebClient webClient;

	UnityOauthClient(@Qualifier(OAUTH_UNITY_CLIENT) WebClient oauthTokenEndpointWebClient) {
		this.webClient = oauthTokenEndpointWebClient;
	}

	public <T> T postForObject(URI uri, Class<T> clazz, Object body, MediaType mediaType) {
		return webClient.post()
				.uri(u -> uri)
				.contentType(mediaType)
				.bodyValue(body == null ? "" : body)
				.retrieve()
				.bodyToMono(clazz)
				.block();
	}

	public void post(URI uri, MediaType mediaType) {
		postForObject(uri, Void.class, null, mediaType);
	}
}
