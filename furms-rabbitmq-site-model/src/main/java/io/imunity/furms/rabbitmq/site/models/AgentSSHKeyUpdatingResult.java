/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("UserSSHKeyUpdateResult")
public class AgentSSHKeyUpdatingResult implements Body {
	public final String fenixUserId;
	public final String uid;

	@JsonCreator
	public AgentSSHKeyUpdatingResult(String fenixUserId, String uid) {
		this.fenixUserId = fenixUserId;
		this.uid = uid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(fenixUserId, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AgentSSHKeyUpdatingResult other = (AgentSSHKeyUpdatingResult) obj;
		return Objects.equals(fenixUserId, other.fenixUserId) && Objects.equals(uid, other.uid);
	}

	@Override
	public String toString() {
		return "AgentSSHKeyUpdatingResult{" + "fenixUserId=" + fenixUserId + ", uid=" + uid + '}';
	}
}
