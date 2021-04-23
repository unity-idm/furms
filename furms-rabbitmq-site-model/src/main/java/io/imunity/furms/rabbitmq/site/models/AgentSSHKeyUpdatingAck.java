/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;


import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessage;


@FurmsMessage(type = "UserSSHKeyUpdateAck")
public class AgentSSHKeyUpdatingAck extends EmptyBodyResponse {
	private AgentSSHKeyUpdatingAck(String correlationId, String status) {
		super(correlationId, status);
	}
}
