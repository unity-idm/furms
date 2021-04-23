/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;


import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessage;

@FurmsMessage(type = "UserSSHKeyAddAck")
public class AgentSSHKeyAdditionAck extends EmptyBodyResponse {
	private AgentSSHKeyAdditionAck(String correlationId, String status) {
		super(correlationId, status);
	}
}
