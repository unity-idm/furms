/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessage;

@JsonDeserialize(builder = AgentSSHKeyAdditionResult.AgentSSHKeyAdditionResultBuilder.class)
@FurmsMessage(type = "UserSSHKeyAddResult")
public class AgentSSHKeyAdditionResult {
	public final String fenixUserId;
	public final String uid;

	AgentSSHKeyAdditionResult(String fenixUserId, String uid) {
		this.fenixUserId = fenixUserId;
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "AgentSSHKeyInstallationResult{" + "fenixUserId=" + fenixUserId + ", uid=" + uid + '}';
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class AgentSSHKeyAdditionResultBuilder {
		private String fenixUserId;
		private String uid;

		private AgentSSHKeyAdditionResultBuilder() {
		}

		public static AgentSSHKeyAdditionResultBuilder anAgentSSHKeyAdditionResultBuilder() {
			return new AgentSSHKeyAdditionResultBuilder();
		}

		public AgentSSHKeyAdditionResultBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public AgentSSHKeyAdditionResultBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public AgentSSHKeyAdditionResult build() {
			return new AgentSSHKeyAdditionResult(fenixUserId, uid);
		}
	}
}
