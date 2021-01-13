/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.roles;

public class RoleLoaderException extends Exception{
	public final String code;

	public RoleLoaderException(int code, Throwable cause) {
		super(cause);
		this.code = String.valueOf(code);
	}
}
