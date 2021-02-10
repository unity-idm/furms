/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class UnityClient {

	private final WebClient webClient;

	public UnityClient(WebClient webClient) {
		this.webClient = webClient;
	}

	public <T> T get(String path, Class<T> clazz) {
		return get(path, clazz, emptyMap());
	}

	public <T> T get(String path, ParameterizedTypeReference<T> typeReference) {
		return get(path, typeReference, emptyMap());
	}

	public <T> T get(String path, Class<T> clazz, Map<String, String> queryParams) {
		MultiValueMap<String, String> params = createStringParams(queryParams);
		return webClient.get()
				.uri(uriBuilder -> uri(uriBuilder, path, params))
				.retrieve()
				.bodyToMono(clazz)
				.block();
	}

	public <T> T getWithListParam(String path, ParameterizedTypeReference<T> typeReference, Map<String, List<String>> queryParams) {
		return get(path, typeReference, createListParams(queryParams));
	}
	
	public <T> T get(String path, ParameterizedTypeReference<T> typeReference, Map<String, String> queryParams) {
		return get(path, typeReference, createStringParams(queryParams));
	}
	
	private <T> T get(String path, ParameterizedTypeReference<T> typeReference, MultiValueMap<String, String> params) {
		return webClient.get()
			.uri(uriBuilder -> uri(uriBuilder, path, params))
			.retrieve()
			.bodyToMono(typeReference)
			.block();
	}

	public void post(String path) {
		post(path, null);
	}

	public void post(String path, Object body) {
		post(path, body, emptyMap());
	}

	public void post(String path, Object body, Map<String, String> queryParams) {
		MultiValueMap<String, String> params = createStringParams(queryParams);
		webClient.post()
				.uri(uriBuilder -> uri(uriBuilder, path, params))
				.bodyValue(body == null ? "" : body)
				.retrieve()
				.bodyToMono(Void.class).block();
	}

	public void post(URI uri, MediaType mediaType) {
		webClient.post()
				.uri(u -> uri)
				.contentType(mediaType)
				.bodyValue("")
				.retrieve()
				.bodyToMono(Void.class).block();
	}

	public void put(String path, Object body) {
		webClient.put()
				.uri(uriBuilder -> uri(uriBuilder, path))
				.bodyValue(body == null ? "" : body)
				.retrieve()
				.bodyToMono(Void.class).block();
	}

	public void put(String path) {
		put(path, null);
	}
	
	public void put(String path, Object body, Map<String, String> queryParams) {
		MultiValueMap<String, String> params = createStringParams(queryParams);
		webClient.put()
				.uri(uriBuilder -> uri(uriBuilder, path, params))
				.bodyValue(body == null ? "" : body)
				.retrieve()
				.bodyToMono(Void.class).block();
	}

	public void delete(String path, Map<String, String> queryParams) {
		MultiValueMap<String, String> params = createStringParams(queryParams);
		webClient.delete()
				.uri(uriBuilder -> uri(uriBuilder, path, params))
				.retrieve()
				.bodyToMono(Void.class).block();
	}

	private MultiValueMap<String, String> createListParams(Map<String, List<String>> queryParams) {
		return new LinkedMultiValueMap<>(queryParams);
	}

	private MultiValueMap<String, String> createStringParams(Map<String, String> queryParams) {
		Map<String, List<String>> mutatedQueryParams = queryParams.entrySet().stream()
				.collect(toMap(entry -> entry.getKey(), entry -> List.of(entry.getValue().toString())));
		return new LinkedMultiValueMap<>(mutatedQueryParams);
	}
	
	private URI uri(final UriBuilder uriBuilder, String path) {
		return uri(uriBuilder, path, null);
	}

	private URI uri(final UriBuilder uriBuilder, String path, MultiValueMap<String, String> queryParams) {
		return UriComponentsBuilder
				.fromUri(uriBuilder.build())
				.path(path)
				.queryParams(queryParams)
				.build(true)
				.toUri();
	}


}
