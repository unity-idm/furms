/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.util.Objects;

public class ErrorMessage {
	public final String code;
	public final String message;

	public ErrorMessage(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ErrorMessage that = (ErrorMessage) o;
		return Objects.equals(code, that.code) 
				&& Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, message);
	}

	@Override
	public String toString() {
		return "ErrorStatus{" +
			"code=" + code +
			", message='" + message + '\'' +
			'}';
	}
}
