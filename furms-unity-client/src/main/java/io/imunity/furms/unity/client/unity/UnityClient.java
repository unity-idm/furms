/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.client.unity;

import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.imunity.furms.unity.client.unity.UriVariableUtils.buildPath;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

@Component
public class UnityClient {

	private final WebClient webClient;

	public UnityClient(WebClient webClient) {
		this.webClient = webClient;
	}

	public <T> T get(String path, Class<T> clazz, Map<String, Object> uriVariables) {
		return get(path, clazz, uriVariables, emptyMap());
	}

	public <T> T get(String path, Class<T> clazz, Map<String, Object> uriVariables, Map<String, Object> queryParams) {
		String expandedPath = buildPath(path, uriVariables);
		MultiValueMap<String, String> params = createParams(queryParams);
		return webClient.get()
				.uri(uriBuilder -> uri(uriBuilder, expandedPath, params))
				.retrieve()
				.bodyToMono(clazz)
				.block();
	}

	public void post(String path, Map<String, Object> uriVariables) {
		post(path, null, uriVariables);
	}

	public void post(String path, Object body, Map<String, Object> uriVariables) {
		String expandedPath = buildPath(path, uriVariables);
		callVoidRequest(webClient.post()
				.uri(uriBuilder -> uri(uriBuilder, expandedPath))
				.bodyValue(body == null ? "" : body));
	}
	public void put(String path, Object body, Map<String, Object> uriVariables) {
		String expandedPath = buildPath(path, uriVariables);
		callVoidRequest(webClient.put()
				.uri(uriBuilder -> uri(uriBuilder, expandedPath))
				.bodyValue(body == null ? "" : body));
	}

	public void delete(String path, Map<String, Object> uriVariables, Map<String, Object> queryParams) {
		String expandedPath = buildPath(path, uriVariables);
		MultiValueMap<String, String> params = createParams(queryParams);
		callVoidRequest(webClient.delete()
						.uri(uriBuilder -> uri(uriBuilder, expandedPath, params)));
	}

	private void callVoidRequest(WebClient.RequestHeadersSpec<?> request) {
		request.retrieve()
				.bodyToMono(Void.class)
				.block();
	}

	private MultiValueMap<String, String> createParams(Map<String, Object> queryParams) {
		Map<String, List<String>> mutatedQueryParams = queryParams.keySet().stream()
				.collect(toMap(Function.identity(),
						key -> List.of(queryParams.get(key).toString())));
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
