/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;


@JsonSubTypes({
	@Type(value = AgentPingAck.class),
	@Type(value = AgentPingRequest.class),
	@Type(value = AgentProjectInstallationAck.class),
	@Type(value = AgentProjectInstallationRequest.class),
	@Type(value = AgentProjectInstallationResult.class),
	@Type(value = AgentSSHKeyAdditionAck.class),
	@Type(value = AgentSSHKeyAdditionRequest.class),
	@Type(value = AgentSSHKeyAdditionResult.class),
	@Type(value = AgentSSHKeyRemovalAck.class),
	@Type(value = AgentSSHKeyRemovalRequest.class),
	@Type(value = AgentSSHKeyRemovalResult.class),
	@Type(value = AgentSSHKeyUpdatingAck.class),
	@Type(value = AgentSSHKeyUpdatingRequest.class),
	@Type(value = AgentSSHKeyUpdatingResult.class)
})
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
public interface Body {
}
