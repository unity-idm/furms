/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.rabbitmq.site.client.message_remover;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.rabbitmq.site.models.Body;
import io.imunity.furms.rabbitmq.site.models.UserAllocationBlockAccessRequest;
import io.imunity.furms.rabbitmq.site.models.UserAllocationGrantAccessRequest;
import io.imunity.furms.site.api.message_remover.ResourceAccessRemover;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class ResourceAccessRemoveStrategy implements PendingMessageRemoveStrategy {
	private final ResourceAccessRemover resourceAccessRemover;
	private final Set<Class<? extends Body>> classes = Set.of(UserAllocationGrantAccessRequest.class, UserAllocationBlockAccessRequest.class);

	ResourceAccessRemoveStrategy(ResourceAccessRemover resourceAccessRemover) {
		this.resourceAccessRemover = resourceAccessRemover;
	}

	@Override
	public boolean isApplicable(String name) {
		return classes.stream()
			.map(x -> x.getAnnotation(JsonTypeName.class).value())
			.anyMatch(x -> x.equals(name));
	}

	@Override
	public void remove(CorrelationId correlationId) {
		resourceAccessRemover.remove(correlationId);
	}
}
