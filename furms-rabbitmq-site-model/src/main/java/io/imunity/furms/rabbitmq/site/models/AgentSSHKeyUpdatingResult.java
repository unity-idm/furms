/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessage;

@JsonDeserialize(builder = AgentSSHKeyUpdatingResult.AgentSSHKeyUpdatingResultBuilder.class)
@FurmsMessage(type = "UserSSHKeyUpdateResult")
public class AgentSSHKeyUpdatingResult {
	public final String fenixUserId;
	public final String uid;

	AgentSSHKeyUpdatingResult(String fenixUserId, String uid) {
		this.fenixUserId = fenixUserId;
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "AgentSSHKeyUpdatingResult{" + "fenixUserId=" + fenixUserId + ", uid=" + uid + '}';
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class AgentSSHKeyUpdatingResultBuilder {
		private String fenixUserId;
		private String uid;

		private AgentSSHKeyUpdatingResultBuilder() {
		}

		public static AgentSSHKeyUpdatingResultBuilder anAgentSSHKeyUpdatingResultBuilder() {
			return new AgentSSHKeyUpdatingResultBuilder();
		}

		public AgentSSHKeyUpdatingResultBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public AgentSSHKeyUpdatingResultBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public AgentSSHKeyUpdatingResult build() {
			return new AgentSSHKeyUpdatingResult(fenixUserId, uid);
		}
	}
}
