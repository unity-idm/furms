/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_remover;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyAdditionRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyRemovalRequest;
import io.imunity.furms.rabbitmq.site.models.AgentSSHKeyUpdatingRequest;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.site.api.message_remover.AgentSSHKeyRemover;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class AgentSSHKeyRemovalStrategy implements PendingMessageRemovalStrategy {
	private final AgentSSHKeyRemover agentSSHKeyRemover;
	private final Set<Class<? extends Body>> classes = Set.of(AgentSSHKeyAdditionRequest.class, AgentSSHKeyRemovalRequest.class, AgentSSHKeyUpdatingRequest.class);

	AgentSSHKeyRemovalStrategy(AgentSSHKeyRemover agentSSHKeyRemover) {
		this.agentSSHKeyRemover = agentSSHKeyRemover;
	}

	@Override
	public boolean isApplicable(String name) {
		return classes.stream()
			.map(x -> x.getAnnotation(JsonTypeName.class).value())
			.anyMatch(x -> x.equals(name));
	}

	@Override
	public void remove(CorrelationId correlationId) {
		agentSSHKeyRemover.remove(correlationId);
	}
}
