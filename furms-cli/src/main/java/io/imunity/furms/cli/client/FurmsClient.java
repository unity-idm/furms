/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.client;

import static io.imunity.furms.cli.client.RestTemplateConfig.createClient;
import static java.util.Optional.of;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;


public class FurmsClient {
	protected final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final RestTemplate webClient;
	private final ObjectMapper objectMapper;

	FurmsClient(FurmsClientBuilder builder) {
		this.webClient = createClient(builder.url, builder.username, builder.apiKey, 
				builder.trustStore, builder.trustStoreType, builder.trustStorePassword);
		this.objectMapper = new ObjectMapper();
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
			LOG.debug("Raw response: {}", response);
			LOG.debug("Result {}", response.getStatusCode());
			System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object));
		} catch (Exception e) {
			LOG.error("Unable to parse and print response: {}", response);
		}
	}

	private void printEmptyResponse() {
		LOG.error("Cannot get response.");
	}
	
	public static class FurmsClientBuilder {
		private String url;
		private String username;
		private String apiKey;
		private String trustStore;
		private String trustStorePassword;
		private String trustStoreType;

		private FurmsClientBuilder() {
		}

		public FurmsClientBuilder url(String url) {
			this.url = url;
			return this;
		}

		public FurmsClientBuilder username(String username) {
			this.username = username;
			return this;
		}

		public FurmsClientBuilder apiKey(String apiKey) {
			this.apiKey = apiKey;
			return this;
		}

		public FurmsClientBuilder trustStore(String trustStore) {
			this.trustStore = trustStore;
			return this;
		}

		public FurmsClientBuilder trustStorePassword(String trustStorePassword) {
			this.trustStorePassword = trustStorePassword;
			return this;
		}
		
		public FurmsClientBuilder trustStoreType(String trustStoreType) {
			this.trustStoreType = trustStoreType;
			return this;
		}

		public FurmsClient build() {
			return new FurmsClient(this);
		}
	}
}
