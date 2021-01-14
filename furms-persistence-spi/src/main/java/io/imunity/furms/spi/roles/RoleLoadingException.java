/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.roles;

public class RoleLoadingException extends RuntimeException {
	public final String code;

	public RoleLoadingException(int code, Throwable cause) {
		super(cause);
		this.code = String.valueOf(code);
	}
}
