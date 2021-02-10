/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.config;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Base64;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
	
	public static final String BASE_UNITY_CLIENT = "baseUnityClient";
	
	@Bean
	@Primary
	public WebClient adminWebClient(UnityProperties unityProperties, SslContextManager sslContextManager) {
		String authorizationKey = createAuthorizationKey(unityProperties.getAdminUser(),
				unityProperties.getAdminPassword());
		return createBuilder(unityProperties.getAdminUrl(), sslContextManager)
				.defaultHeader(AUTHORIZATION, authorizationKey)
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
	}
	
	@Bean(name=BASE_UNITY_CLIENT)
	public WebClient webClient(UnityProperties unityProperties, SslContextManager sslContextManager) {
		return createBuilder(unityProperties.getUrl(), sslContextManager)
				.build();
	}
	
	private WebClient.Builder createBuilder(String baseUrl, SslContextManager sslContextManager) {
		HttpClient httpClient = HttpClient.create()
				.secure(sslSpec -> sslSpec.sslContext(sslContextManager.getSslContextForWebClient()));
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.baseUrl(baseUrl);
	}

	private String createAuthorizationKey(String username, String password) {
		byte[] userPass = format("%s:%s", username, password).getBytes();
		String token = Base64.getEncoder().encodeToString(userPass);
		return format("Basic %s", token);
	}

}
