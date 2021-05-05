/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonTypeName("UserSSHKeyUpdatingRequest")
public class AgentSSHKeyUpdatingRequest implements Body {
	public final String fenixUserId;
	public final String oldPublicKey;
	public final String newPublicKey;

	AgentSSHKeyUpdatingRequest(String fenixUserId, String oldPublicKey, String newPublicKey) {

		this.fenixUserId = fenixUserId;
		this.oldPublicKey = oldPublicKey;
		this.newPublicKey = newPublicKey;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, oldPublicKey, newPublicKey);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentSSHKeyUpdatingRequest other = (AgentSSHKeyUpdatingRequest) obj;
		return Objects.equals(fenixUserId, other.fenixUserId)
				&& Objects.equals(oldPublicKey, other.oldPublicKey);
	}

	@Override
	public String toString() {
		return "AgentSSHKeyInstallationRequest{" + "fenixUserId='" + fenixUserId + ", oldPublicKey='"
				+ oldPublicKey + ", newPublicKey='" + newPublicKey + "}";
	}

	public static SSHKeyUpdatingRequestBuilder builder() {
		return new SSHKeyUpdatingRequestBuilder();
	}

	@JsonPOJOBuilder(withPrefix = "")
	public static final class SSHKeyUpdatingRequestBuilder {
		private String fenixUserId;
		private String oldPublicKey;
		private String newPublicKey;

		private SSHKeyUpdatingRequestBuilder() {
		}

		public SSHKeyUpdatingRequestBuilder fenixUserId(String fenixUserId) {
			this.fenixUserId = fenixUserId;
			return this;
		}

		public SSHKeyUpdatingRequestBuilder oldPublicKey(String oldPublicKey) {
			this.oldPublicKey = oldPublicKey;
			return this;
		}

		public SSHKeyUpdatingRequestBuilder newPublicKey(String newPublicKey) {
			this.newPublicKey = newPublicKey;
			return this;
		}

		public AgentSSHKeyUpdatingRequest build() {
			return new AgentSSHKeyUpdatingRequest(fenixUserId, oldPublicKey, newPublicKey);
		}
	}
}
