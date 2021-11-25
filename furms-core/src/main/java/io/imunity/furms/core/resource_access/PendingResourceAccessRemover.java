/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_remover.ResourceAccessRemover;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.springframework.stereotype.Component;

@Component
class PendingResourceAccessRemover implements ResourceAccessRemover {
	private final ResourceAccessRepository resourceAccessRepository;

	PendingResourceAccessRemover(ResourceAccessRepository resourceAccessRepository) {
		this.resourceAccessRepository = resourceAccessRepository;
	}

	@Override
	public void remove(CorrelationId correlationId) {
		resourceAccessRepository.deleteByCorrelationId(correlationId);
	}
}
