/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.exceptions;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyMap;

public class UnityFailureException extends RuntimeException {

	private final int statusCode;
	private final String statusText;
	private final Map<String, List<String>> headers;
	private final String requestBody;
	private final String responseBody;

	public UnityFailureException(String message, int statusCode, String statusText, Map<String, List<String>> headers, String requestBody, String responseBody) {
		super(message);
		this.statusCode = statusCode;
		this.statusText = statusText;
		this.headers = headers != null ? headers : emptyMap();
		this.requestBody = requestBody;
		this.responseBody = responseBody;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusText() {
		return statusText;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public String getRequestBody() {
		return requestBody;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UnityFailureException that = (UnityFailureException) o;
		return statusCode == that.statusCode &&
				Objects.equals(statusText, that.statusText) &&
				Objects.equals(headers, that.headers) &&
				Objects.equals(responseBody, that.responseBody) &&
				Objects.equals(requestBody, that.requestBody);
	}

	@Override
	public int hashCode() {
		return Objects.hash(statusCode, statusText, headers, responseBody, requestBody);
	}

	@Override
	public String toString() {
		return "UnityFailureException{" +
				"statusCode=" + statusCode +
				", statusText='" + statusText + '\'' +
				", headers=" + headers +
				", responseBody='" + responseBody + '\'' +
				", requestBody='" + requestBody + '\'' +
				'}';
	}
}
