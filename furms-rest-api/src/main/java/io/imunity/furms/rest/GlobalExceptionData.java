/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

class GlobalExceptionData {

	private final String message;
	private final String error;

	@JsonCreator
	GlobalExceptionData(@JsonProperty("message") String message, @JsonProperty("error")String error) {
		this.message = message;
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public String getError() {
		return error;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GlobalExceptionData that = (GlobalExceptionData) o;
		return Objects.equals(message, that.message) &&
				Objects.equals(error, that.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message, error);
	}

	@Override
	public String toString() {
		return "GlobalExceptionData{" +
				"message='" + message + '\'' +
				", error='" + error + '\'' +
				'}';
	}
}
