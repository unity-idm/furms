/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

public abstract class EmptyBodyResponse {
	public final String correlationId;
	public final String status;

	EmptyBodyResponse(String correlationId, String status) {
		this.correlationId = correlationId;
		this.status = status;
	}
}
