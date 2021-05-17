/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_resolver.UserAllocationMessageResolver;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
class UserAllocationMessageResolverImpl implements UserAllocationMessageResolver {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ResourceAccessRepository repository;

	UserAllocationMessageResolverImpl(ResourceAccessRepository repository) {
		this.repository = repository;
	}

	@Override
	public void update(CorrelationId correlationId, AccessStatus status, String msg) {
		AccessStatus currentStatus = repository.findCurrentStatus(correlationId);
		if(!currentStatus.isTransitionalTo(status))
			throw new IllegalArgumentException(String.format("Transit between %s and %s states doesn't exist", currentStatus, status));
		if(status.equals(AccessStatus.REVOKED)) {
			repository.delete(correlationId);
			LOG.info("UserAllocation with correlation id {} was removed", correlationId.id);
			return;
		}
		repository.update(correlationId, status, msg);
		LOG.info("UserAllocation status with correlation id {} was updated {}", correlationId.id, status);
	}
}
