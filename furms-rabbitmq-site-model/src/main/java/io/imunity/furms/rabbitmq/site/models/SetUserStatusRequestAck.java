/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("SetUserStatusRequestAck")
public class SetUserStatusRequestAck implements Body, Ack {
	@Override
	public String toString() {
		return "SetUserStatusRequestAck{}";
	}
}
