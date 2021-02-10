/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.cidp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.imunity.furms.domain.users.UserStatus;

public class UserStatusHolder {
	public final UserStatus status;

	@JsonCreator
	public UserStatusHolder(@JsonProperty("status") UserStatus status) {
		this.status = status;
	}
}
