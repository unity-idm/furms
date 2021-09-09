/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.cli.client.RestTemplateConfig.createClient;
import static java.util.Optional.of;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;


public class FurmsClient {
	protected final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final RestTemplate webClient;
	private final ObjectMapper objectMapper;
	private final boolean silentMode;

	FurmsClient(String url,
	            String username,
	            String apiKey,
	            String trustStore,
	            String trustStorePassword,
	            boolean silentMode) {
		this.webClient = createClient(url, username, apiKey, trustStore, trustStorePassword);
		this.objectMapper = new ObjectMapper();
		this.silentMode = silentMode;
	}

	public static FurmsClientBuilder builder() {
		return new FurmsClientBuilder();
	}

	public void get(FurmsClientRequest request) {
		LOG.debug("Executing {} with client {}", request, webClient);
		of(webClient
				.getForEntity(request.getPath(), String.class))
				.ifPresentOrElse(this::printResponse, this::printEmptyResponse);

	}

	public void post(FurmsClientRequest request) {
		LOG.debug("Executing {} with client {}", request, webClient);
		of(webClient
				.exchange(request.getPath(), POST, entity(request.getBody()), String.class))
				.ifPresentOrElse(this::printResponse, this::printEmptyResponse);
	}

	public void put(FurmsClientRequest request) {
		LOG.debug("Executing {} with client {}", request, webClient);
		of(webClient
				.exchange(request.getPath(), PUT, entity(request.getBody()), String.class))
				.ifPresentOrElse(this::printResponse, this::printEmptyResponse);
	}

	public void delete(FurmsClientRequest request) {
		LOG.debug("Executing {} with client {}", request, webClient);
		of(webClient
				.exchange(request.getPath(), DELETE, entity(request.getBody()), String.class))
				.ifPresentOrElse(this::printResponse, this::printEmptyResponse);
	}

	private HttpEntity<String> entity(String body) {
		return new HttpEntity<>(body);
	}

	private void printResponse(ResponseEntity<String> response) {
		try {
			final Object object = objectMapper.readValue(response.getBody(), Object.class);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Response: {}", response);
			} else {
				LOG.info("Response: {}\n{}",
						response.getStatusCode(),
						objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object));
			}
			if (silentMode) {
				System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object));
			}
		} catch (Exception e) {
			LOG.error("Unable to parse and print response: {}", response);
		}
	}

	private void printEmptyResponse() {
		LOG.error("Cannot get response.");
	}
}
