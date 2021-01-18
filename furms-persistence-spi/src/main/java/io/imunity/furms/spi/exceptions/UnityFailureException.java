/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.exceptions;

public class UnityFailureException extends RuntimeException {

	public UnityFailureException(String message, Throwable cause) {
		super(message, cause);
	}
}
