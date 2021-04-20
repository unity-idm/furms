/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;


import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessage;

@FurmsMessage(type = "ProjectInstallationAck")
public class AgentProjectInstallationAck extends EmptyBodyResponse {
	private AgentProjectInstallationAck(String correlationId, String status) {
		super(correlationId, status);
	}
}