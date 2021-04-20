/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import static io.imunity.furms.rabbitmq.site.models.Payload.PayloadBuilder;

@JsonDeserialize(builder = PayloadBuilder.class)
public class Payload {
	public final Header header;
	public final Body body;
	Payload(Header header, Body body) {
		this.header = header;
		this.body = body;
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class PayloadBuilder {
		public Header header;
		public Body body;

		private PayloadBuilder() {
		}

		public static PayloadBuilder aPayload() {
			return new PayloadBuilder();
		}

		public PayloadBuilder header(Header header) {
			this.header = header;
			return this;
		}

		public PayloadBuilder body(Body body) {
			this.body = body;
			return this;
		}

		public Payload build() {
			return new Payload(header, body);
		}
	}
}
