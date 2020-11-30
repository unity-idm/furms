/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.config;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

import static java.util.Arrays.asList;

@Configuration
class RestTemplateUnityConfiguration {
	@Value("${unity.client.ssl.trust-store}")
	private Resource keyStore;
	@Value("${unity.client.ssl.trust-store-password}")
	private String keyStorePassword;

	@Bean
	public RestTemplate getRestClientForUnityConnections() {
		SSLContext sslContext = getSslContext();
		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
		HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
		RestTemplate restTemplate = new RestTemplate(factory);
		restTemplate.setMessageConverters(
			asList(
				new FormHttpMessageConverter(),
				new OAuth2AccessTokenResponseHttpMessageConverter(),
				new MappingJackson2HttpMessageConverter()
			));
		restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
		return restTemplate;
	}

	private SSLContext getSslContext() {
		try {
			return new SSLContextBuilder()
				.loadTrustMaterial(keyStore.getFile(), keyStorePassword.toCharArray())
				.build();
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
