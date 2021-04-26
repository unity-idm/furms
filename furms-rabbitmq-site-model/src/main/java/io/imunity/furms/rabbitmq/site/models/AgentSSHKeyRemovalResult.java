/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("UserSSHKeyRemovalResult")
public class AgentSSHKeyRemovalResult implements Body{
	public final String fenixUserId;
	public final String uid;

	@JsonCreator
	public AgentSSHKeyRemovalResult(String fenixUserId, String uid) {
		this.fenixUserId = fenixUserId;
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "UserSSHKeyRemovalResult{" + "fenixUserId=" + fenixUserId + ", uid=" + uid + '}';
	}
}
