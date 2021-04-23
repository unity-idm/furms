/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class Header {
	public final String version;
	public final String messageCorrelationId;
	@JsonInclude(NON_NULL)
	public final Status status;
	@JsonInclude(NON_NULL)
	public final Error error;

	@JsonCreator
	public Header( String version, String messageCorrelationId, Status status, Error error) {
		this.version = version;
		this.messageCorrelationId = messageCorrelationId;
		this.status = status;
		this.error = error;
	}

	public Header(String version, String messageCorrelationId) {
		this(version, messageCorrelationId, null, null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Header header = (Header) o;
		return Objects.equals(version, header.version) &&
			Objects.equals(messageCorrelationId, header.messageCorrelationId) &&
			Objects.equals(status, header.status) &&
			Objects.equals(error, header.error);
	}

	@Override
	public int hashCode() {
		return Objects.hash(version, messageCorrelationId, status, error);
	}

	@Override
	public String toString() {
		return "Header{" +
			"version='" + version + '\'' +
			", messageCorrelationId='" + messageCorrelationId + '\'' +
			", status='" + status + '\'' +
			", error=" + error +
			'}';
	}
}
