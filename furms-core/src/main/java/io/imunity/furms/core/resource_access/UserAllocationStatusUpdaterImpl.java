/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.core.user_operation.UserOperationService;
import io.imunity.furms.domain.policy_documents.UserWaitingPoliciesAcceptanceListChangedEvent;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.ProjectUserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.status_updater.UserAllocationStatusUpdater;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;

@Service
class UserAllocationStatusUpdaterImpl implements UserAllocationStatusUpdater {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ResourceAccessRepository repository;
	private final UserOperationService userOperationService;
	private final ApplicationEventPublisher publisher;


	UserAllocationStatusUpdaterImpl(ResourceAccessRepository repository, UserOperationService userOperationService,
	                                ApplicationEventPublisher publisher) {
		this.repository = repository;
		this.userOperationService = userOperationService;
		this.publisher = publisher;
	}

	@Override
	@Transactional
	public void update(CorrelationId correlationId, AccessStatus status, String msg) {
		AccessStatus currentStatus = repository.findCurrentStatus(correlationId);
		if(!currentStatus.isTransitionalTo(status))
			throw new IllegalArgumentException(String.format("Transition between %s and %s states is not allowed", currentStatus, status));
		if(status.equals(AccessStatus.REVOKED)) {
			ProjectUserGrant projectUserGrant = repository.findUsersGrantsByCorrelationId(correlationId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Resource access correlation Id %s doesn't exist", correlationId)));
			if(repository.findUserGrantsByProjectIdAndFenixUserId(projectUserGrant.projectId, projectUserGrant.userId).isEmpty())
				userOperationService.createUserRemovals(projectUserGrant.projectId, projectUserGrant.userId);
			repository.deleteByCorrelationId(correlationId);
			LOG.info("UserAllocation with correlation id {} was removed", correlationId.id);
			return;
		}
		repository.update(correlationId, status, msg);
		if(status.equals(AccessStatus.GRANTED)){
			ProjectUserGrant projectUserGrant = repository.findUsersGrantsByCorrelationId(correlationId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Resource access correlation Id %s doesn't exist", correlationId)));
			publisher.publishEvent(new UserWaitingPoliciesAcceptanceListChangedEvent(projectUserGrant.userId));
		}
		LOG.info("UserAllocation status with correlation id {} was updated to {}", correlationId.id, status);
	}
}
