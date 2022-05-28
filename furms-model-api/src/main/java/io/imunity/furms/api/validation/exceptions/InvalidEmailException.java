/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class InvalidEmailException extends IllegalArgumentException {

	public InvalidEmailException(String message) {
		super(message);
	}
}
