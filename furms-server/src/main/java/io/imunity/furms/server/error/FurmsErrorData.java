/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.server.error;

import java.util.Objects;

class FurmsErrorData {

	private final String message;
	private final String error;
	private final String path;

	FurmsErrorData(String message, String error, String path) {
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
		FurmsErrorData that = (FurmsErrorData) o;
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
		return "FurmsErrorData{" +
				"message='" + message + '\'' +
				", error='" + error + '\'' +
				", path='" + path + '\'' +
				'}';
	}

	public static FurmsErrorDataBuilder builder() {
		return new FurmsErrorDataBuilder();
	}

	public static final class FurmsErrorDataBuilder {
		private String message;
		private String error;
		private String path;

		private FurmsErrorDataBuilder() {
		}

		public FurmsErrorDataBuilder message(String message) {
			this.message = message;
			return this;
		}

		public FurmsErrorDataBuilder error(String error) {
			this.error = error;
			return this;
		}

		public FurmsErrorDataBuilder path(String path) {
			this.path = path;
			return this;
		}

		public FurmsErrorData build() {
			return new FurmsErrorData(message, error, path);
		}
	}
}
