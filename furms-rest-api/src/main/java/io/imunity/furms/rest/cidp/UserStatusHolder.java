/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.cidp;

import io.imunity.furms.domain.users.UserStatus;

public class UserStatusHolder {
	public final UserStatus status;

	public UserStatusHolder(UserStatus status) {
		this.status = status;
	}
}
