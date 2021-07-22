/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz.roles;

public class IncorrectResourceIdException extends RuntimeException {

	public IncorrectResourceIdException(String message) {
		super(message);
	}
}
