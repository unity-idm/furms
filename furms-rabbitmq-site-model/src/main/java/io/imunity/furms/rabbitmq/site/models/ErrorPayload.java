/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import java.util.Objects;

public class ErrorPayload {
	public final String correlationId;
	public final String unparsableMessage;

	public ErrorPayload(String correlationId, String unparsableMessage) {
		this.correlationId = correlationId;
		this.unparsableMessage = unparsableMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ErrorPayload that = (ErrorPayload) o;
		return Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(unparsableMessage, that.unparsableMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(correlationId, unparsableMessage);
	}

	@Override
	public String toString() {
		return "ErrorPayload{" +
			"correlationId='" + correlationId + '\'' +
			", unparsableMessage='" + unparsableMessage + '\'' +
			'}';
	}
}
