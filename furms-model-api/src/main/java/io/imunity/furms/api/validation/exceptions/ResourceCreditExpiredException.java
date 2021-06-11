/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class ResourceCreditExpiredException extends IllegalArgumentException {

	public ResourceCreditExpiredException(String message) {
		super(message);
	}
}
