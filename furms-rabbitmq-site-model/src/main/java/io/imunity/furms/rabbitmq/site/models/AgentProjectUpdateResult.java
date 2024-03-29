/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ProjectUpdateResult")
public class AgentProjectUpdateResult implements Body, Result {
	@Override
	public String toString() {
		return "AgentProjectUpdateResult{}";
	}
}
