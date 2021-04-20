/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import static io.imunity.furms.rabbitmq.site.models.AgentPingAckTest.AgentPingAckTestBuilder;

@JsonDeserialize(builder = AgentPingAckTestBuilder.class)
//@JsonTypeName("AgentPingAck")
public class AgentPingAckTest extends Body {
	public final String name;
	public final int count;

	AgentPingAckTest(String name, int count) {
		this.name = name;
		this.count = count;
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class AgentPingAckTestBuilder {
		public String name;
		public int count;

		private AgentPingAckTestBuilder() {
		}

		public static AgentPingAckTestBuilder anAgentPingAckTest() {
			return new AgentPingAckTestBuilder();
		}

		public AgentPingAckTestBuilder name(String name) {
			this.name = name;
			return this;
		}

		public AgentPingAckTestBuilder count(int count) {
			this.count = count;
			return this;
		}

		public AgentPingAckTest build() {
			return new AgentPingAckTest(name, count);
		}
	}
}
