/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.cli.client;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class FurmsClientRequest {

	private final String path;
	private final String body;
	private final List<String> pathParams;
	private final MultiValueMap<String, String> queryParams;

	private FurmsClientRequest(String path,
	                           String body,
	                           List<String> pathParams,
	                           MultiValueMap<String, String> queryParams) {
		this.path = path;
		this.body = body;
		this.pathParams = pathParams;
		this.queryParams = queryParams;
	}

	public String getBody() {
		return body;
	}

	public String getPath() {
		return UriComponentsBuilder
				.fromPath(path)
				.queryParams(queryParams)
				.buildAndExpand(pathParams.toArray(Object[]::new))
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FurmsClientRequest that = (FurmsClientRequest) o;
		return Objects.equals(path, that.path) && Objects.equals(body, that.body) && Objects.equals(pathParams, that.pathParams) && Objects.equals(queryParams, that.queryParams);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, body, pathParams, queryParams);
	}

	@Override
	public String toString() {
		return "Request{" +
				"path='" + path + '\'' +
				", body='" + body + '\'' +
				", pathParams=" + pathParams +
				", queryParams=" + queryParams +
				'}';
	}

	public static FurmsClientRequestBuilder path(String path) {
		return new FurmsClientRequestBuilder(path);
	}

	public static final class FurmsClientRequestBuilder {
		private String path;
		private String body = "";
		private List<String> pathParams = List.of();
		private MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>(Map.of());

		private FurmsClientRequestBuilder(String path) {
			this.path = path;
		}

		public FurmsClientRequestBuilder body(String body) {
			this.body = body;
			return this;
		}

		public FurmsClientRequestBuilder pathParams(String... pathParams) {
			this.pathParams = List.of(pathParams);
			return this;
		}

		public FurmsClientRequestBuilder queryParams(Map<String, String> queryParams) {
			this.queryParams = new LinkedMultiValueMap<>(queryParams.entrySet().stream()
					.filter(entry -> entry.getValue() != null)
					.collect(toMap(
							Map.Entry::getKey,
							entry -> List.of(entry.getValue()))));
			return this;
		}

		public FurmsClientRequest build() {
			return new FurmsClientRequest(path, body, pathParams, queryParams);
		}
	}
}
