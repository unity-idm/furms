/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName("AgentMessageErrorInfo")
public class AgentMessageErrorInfo implements Body {
	public final String correlationId;
	public final String errorType;
	public final String description;

	@JsonCreator
	public AgentMessageErrorInfo(String correlationId, String errorType, String description) {
		this.correlationId = correlationId;
		this.errorType = errorType;
		this.description = description;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AgentMessageErrorInfo that = (AgentMessageErrorInfo) o;
		return Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(errorType, that.errorType) &&
			Objects.equals(description, that.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(correlationId, errorType, description);
	}

	@Override
	public String toString() {
		return "AgentMessageErrorInfo{" +
			"correlationId='" + correlationId + '\'' +
			", errorType='" + errorType + '\'' +
			", description='" + description + '\'' +
			'}';
	}
}
