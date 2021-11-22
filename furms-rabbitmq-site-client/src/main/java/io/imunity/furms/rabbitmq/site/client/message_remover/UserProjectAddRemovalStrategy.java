/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_remover;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.UserProjectAddRequest;
import io.imunity.furms.rabbitmq.site.models.UserProjectRemovalRequest;
import io.imunity.furms.site.api.message_remover.UserProjectAddRemover;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class UserProjectAddRemovalStrategy implements PendingMessageRemovalStrategy {
	private final UserProjectAddRemover userProjectAddRemover;
	private final Set<Class<? extends Body>> classes = Set.of(UserProjectAddRequest.class, UserProjectRemovalRequest.class);

	UserProjectAddRemovalStrategy(UserProjectAddRemover userProjectAddRemover) {
		this.userProjectAddRemover = userProjectAddRemover;
	}

	@Override
	public boolean isApplicable(String name) {
		return classes.stream()
			.map(x -> x.getAnnotation(JsonTypeName.class).value())
			.anyMatch(x -> x.equals(name));
	}

	@Override
	public void remove(CorrelationId correlationId) {
		userProjectAddRemover.remove(correlationId);
	}
}
