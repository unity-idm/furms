/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.sites.exceptions;

public class IdNotFoundValidationError extends IllegalArgumentException {

	public IdNotFoundValidationError(String message) {
		super(message);
	}
}
