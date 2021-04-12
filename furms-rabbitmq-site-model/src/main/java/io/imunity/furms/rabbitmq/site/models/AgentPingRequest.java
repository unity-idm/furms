/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessage;

@FurmsMessage(type = "AgentPingRequest")
public class AgentPingRequest extends EmptyBodyResponse {
	private AgentPingRequest(String correlationId, String status) {
		super(correlationId, status);
	}
}
