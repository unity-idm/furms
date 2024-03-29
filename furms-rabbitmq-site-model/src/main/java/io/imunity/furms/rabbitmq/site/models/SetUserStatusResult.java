/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("SetUserStatusResult")
public class SetUserStatusResult implements Body, Result {
	@Override
	public String toString() {
		return "SetUserStatusResult{}";
	}
}
