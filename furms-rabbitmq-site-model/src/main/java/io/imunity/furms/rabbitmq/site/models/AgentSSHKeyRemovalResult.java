/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import io.imunity.furms.rabbitmq.site.models.converter.FurmsMessage;

@JsonDeserialize(builder = AgentSSHKeyRemovalResult.AgentSSHKeyRemovalResultBuilder.class)
@FurmsMessage(type = "UserSSHKeyRemovalResult")
public class AgentSSHKeyRemovalResult {
	public final String fenixUserId;
	public final String uid;

	AgentSSHKeyRemovalResult(String fenixUserId, String uid) {
		super();
		this.fenixUserId = fenixUserId;
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "UserSSHKeyRemovalResult{" + "fenixUserId=" + fenixUserId + ", uid=" + uid + '}';
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class AgentSSHKeyRemovalResultBuilder {
		private String fenixUserId;
		private String uid;

		private AgentSSHKeyRemovalResultBuilder() {
		}

		public static AgentSSHKeyRemovalResultBuilder anAgentSSHKeyRemovalResultBuilder() {
			return new AgentSSHKeyRemovalResultBuilder();
		}

		public AgentSSHKeyRemovalResultBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public AgentSSHKeyRemovalResultBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public AgentSSHKeyRemovalResult build() {
			return new AgentSSHKeyRemovalResult(fenixUserId, uid);
		}
	}
}
