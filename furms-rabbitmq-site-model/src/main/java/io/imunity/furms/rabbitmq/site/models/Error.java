/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Objects;

public class Error {
	public final String code;
	public final String message;

	@JsonCreator
	public Error(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Error error = (Error) o;
		return Objects.equals(code, error.code) &&
			Objects.equals(message, error.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, message);
	}

	@Override
	public String toString() {
		return "Error{" +
			"code='" + code + '\'' +
			", message='" + message + '\'' +
			'}';
	}
}
