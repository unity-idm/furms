/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.domain.users;

public class UnknownUserException extends RuntimeException {
	public final String userId;

	public UnknownUserException(String userId) {
		this.userId = userId;
	}
}