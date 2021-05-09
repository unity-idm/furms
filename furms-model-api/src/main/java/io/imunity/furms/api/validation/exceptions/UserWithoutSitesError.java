/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class UserWithoutSitesError extends IllegalArgumentException {

	public UserWithoutSitesError(String message) {
		super(message);
	}
}
