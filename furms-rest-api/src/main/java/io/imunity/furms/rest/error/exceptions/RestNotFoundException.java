/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.error.exceptions;

public abstract class RestNotFoundException extends RuntimeException {

	public RestNotFoundException(String message) {
		super(message);
	}
}
