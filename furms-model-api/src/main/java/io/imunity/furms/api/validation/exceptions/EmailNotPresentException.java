/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.api.validation.exceptions;

public class EmailNotPresentException extends IllegalArgumentException {
	public final String email;

	public EmailNotPresentException(String email) {
		this.email = email;
	}
}
