/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import java.util.Objects;

public class ErrorAccessMessage {
	public final String message;

	public ErrorAccessMessage(String message) {
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ErrorAccessMessage that = (ErrorAccessMessage) o;
		return Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message);
	}

	@Override
	public String toString() {
		return "ErrorMessage{" +
			"message='" + message + '\'' +
			'}';
	}
}
