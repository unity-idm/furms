/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonTypeName("UserSSHKeyRemovalRequest")
public class AgentSSHKeyRemovalRequest implements Body{
	public final String fenixUserId;
	public final String uid;
	public final String publicKey;

	@JsonCreator
	public AgentSSHKeyRemovalRequest(String fenixUserId, String uid, String publicKey) {

		this.fenixUserId = fenixUserId;
		this.uid = uid;
		this.publicKey = publicKey;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, publicKey, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentSSHKeyRemovalRequest other = (AgentSSHKeyRemovalRequest) obj;
		return Objects.equals(fenixUserId, other.fenixUserId) && Objects.equals(publicKey, other.publicKey)
				&& Objects.equals(uid, other.uid);
	}

	@Override
	public String toString() {
		return "AgentSSHKeyRemovalRequest{" + "fenixUserId='" + fenixUserId + '\'' + ", uid='" + uid + '\''
				+ ", publicKey='" + publicKey + "}";
	}

	public static SSHKeyRemovalRequestBuilder builder() {
		return new SSHKeyRemovalRequestBuilder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class SSHKeyRemovalRequestBuilder {
		private String fenixUserId;
		private String uid;
		private String publicKey;

		private SSHKeyRemovalRequestBuilder() {
		}

		public SSHKeyRemovalRequestBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public SSHKeyRemovalRequestBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public SSHKeyRemovalRequestBuilder publicKey(String publicKey) {
			this.publicKey = publicKey;
			return this;
		}

		public AgentSSHKeyRemovalRequest build() {
			return new AgentSSHKeyRemovalRequest(fenixUserId, uid, publicKey);
		}
	}
}
