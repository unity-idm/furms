/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;


import com.fasterxml.jackson.annotation.JsonTypeName;


@JsonTypeName("UserSSHKeyUpdateRequestAck")
public class AgentSSHKeyUpdatingAck implements Body, Ack {
	@Override
	public String toString() {
		return "AgentSSHKeyUpdatingAck{}";
	}
}
