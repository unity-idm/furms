/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.client;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import javax.net.ssl.SSLContext;
import java.util.Base64;

import static io.imunity.furms.cli.client.SSLContextManager.createSSLContext;
import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class RestTemplateConfig {

	public final static String FURMS_REST_BASE_PATH = "/rest-api/v1";

	static RestTemplate createClient(String url,
	                                 String username,
	                                 String apiKey,
	                                 String trustStore,
	                                 String trustStorePassword) {
		SSLContext sslContext = createSSLContext(trustStore, trustStorePassword);
		SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
		HttpClient httpClient = HttpClients.custom().setSSLSocketFactory(socketFactory).build();

		final RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
		restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(url + FURMS_REST_BASE_PATH));

		restTemplate.getInterceptors().add((request, bytes, execution) -> {
			request.getHeaders().add(AUTHORIZATION, createAuthorizationKey(username, apiKey));
			request.getHeaders().remove(CONTENT_TYPE);
			request.getHeaders().add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
			return execution.execute(request, bytes);
		});

		return restTemplate;
	}

	private static String createAuthorizationKey(String username, String password) {
		byte[] userPass = format("%s:%s", username, password).getBytes();
		String token = Base64.getEncoder().encodeToString(userPass);
		return format("Basic %s", token);
	}

}
