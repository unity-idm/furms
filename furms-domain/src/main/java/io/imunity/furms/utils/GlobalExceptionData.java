/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.utils;

import java.util.Objects;

public class GlobalExceptionData {

	private final String message;
	private final String error;
	private final String path;

	public GlobalExceptionData(String message, String error, String path) {
		this.message = message == null ? error : message;
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
		GlobalExceptionData that = (GlobalExceptionData) o;
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
		return "GlobalExceptionData{" +
				"message='" + message + '\'' +
				", error='" + error + '\'' +
				", path='" + path + '\'' +
				'}';
	}

	public static GlobalExceptionDataBuilder builder() {
		return new GlobalExceptionDataBuilder();
	}

	public static final class GlobalExceptionDataBuilder {
		private String message;
		private String error;
		private String path;

		private GlobalExceptionDataBuilder() {
		}

		public GlobalExceptionDataBuilder message(String message) {
			this.message = message;
			return this;
		}

		public GlobalExceptionDataBuilder error(String error) {
			this.error = error;
			return this;
		}

		public GlobalExceptionDataBuilder path(String path) {
			this.path = path;
			return this;
		}

		public GlobalExceptionData build() {
			return new GlobalExceptionData(message, error, path);
		}
	}
}
