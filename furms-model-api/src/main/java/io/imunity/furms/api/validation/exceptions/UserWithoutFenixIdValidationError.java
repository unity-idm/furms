/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class UserWithoutFenixIdValidationError extends IllegalArgumentException {
	public UserWithoutFenixIdValidationError() {
	}

	public UserWithoutFenixIdValidationError(String message) {
		super(message);
	}
}
