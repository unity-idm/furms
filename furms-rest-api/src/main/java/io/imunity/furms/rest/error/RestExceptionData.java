/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.error;

import java.util.Objects;

class RestExceptionData {

	private final String message;
	private final String error;
	private final String path;

	RestExceptionData(String message, String error, String path) {
		this.message = message;
		this.error = error;
		this.path = path;
	}

	public String getMessage() {
		return message;
	}

	public String getError() {
		return error;
	}

	public String getPath() {
		return path;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RestExceptionData that = (RestExceptionData) o;
		return Objects.equals(message, that.message) &&
				Objects.equals(error, that.error) &&
				Objects.equals(path, that.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message, error, path);
	}

	@Override
	public String toString() {
		return "RestExceptionData{" +
				"message='" + message + '\'' +
				", error='" + error + '\'' +
				", path='" + path + '\'' +
				'}';
	}

	static RestExceptionDataBuilder builder() {
		return new RestExceptionDataBuilder();
	}

	static final class RestExceptionDataBuilder {
		private String message;
		private String error;
		private String path;

		private RestExceptionDataBuilder() {
		}

		public static RestExceptionDataBuilder aRestExceptionData() {
			return new RestExceptionDataBuilder();
		}

		public RestExceptionDataBuilder message(String message) {
			this.message = message;
			return this;
		}

		public RestExceptionDataBuilder error(String error) {
			this.error = error;
			return this;
		}

		public RestExceptionDataBuilder path(String path) {
			this.path = path;
			return this;
		}

		public RestExceptionData build() {
			return new RestExceptionData(message, error, path);
		}
	}
}
