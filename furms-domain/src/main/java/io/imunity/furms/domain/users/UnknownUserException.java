/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.domain.users;

public class UnknownUserException extends RuntimeException {
	public final FenixUserId userId;

	public UnknownUserException(FenixUserId userId) {
		this.userId = userId;
	}
}
