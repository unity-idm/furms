/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class UserNotExistError extends IllegalArgumentException {

	public UserNotExistError(String s) {
		super(s);
	}
}
