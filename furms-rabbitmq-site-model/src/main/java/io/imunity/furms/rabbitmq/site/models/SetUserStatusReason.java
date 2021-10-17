/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SetUserStatusReason {

	SECURITY_INCIDENT ("security_incident");

	@JsonValue
	private final String message;

	SetUserStatusReason(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
