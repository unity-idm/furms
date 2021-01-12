/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity.config;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	@Bean
	public RestTemplate getRestClientForUnityConnections(SslContextManager sslContextManager) {
		SSLContext sslContext = sslContextManager.getSslContextForRestTemplate();
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
}
