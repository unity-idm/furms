/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.resource_access;

import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.core.user_site_access.UserSiteAccessInnerService;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.ProjectUserGrant;
import io.imunity.furms.domain.resource_access.UserGrantRemovedEvent;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.IllegalStateTransitionException;
import io.imunity.furms.domain.site_agent.InvalidCorrelationIdException;
import io.imunity.furms.site.api.status_updater.UserAllocationStatusUpdater;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;

@Service
class UserAllocationStatusUpdaterImpl implements UserAllocationStatusUpdater {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ResourceAccessRepository repository;
	private final UserSiteAccessInnerService userSiteAccessInnerService;
	private final PostCommitRunner postCommitRunner;

	UserAllocationStatusUpdaterImpl(ResourceAccessRepository repository,
	                                UserSiteAccessInnerService userSiteAccessInnerService, PostCommitRunner postCommitRunner) {
		this.repository = repository;
		this.userSiteAccessInnerService = userSiteAccessInnerService;
		this.postCommitRunner = postCommitRunner;
	}

	@Override
	@Transactional
	public void update(CorrelationId correlationId, AccessStatus status, String msg) {
		AccessStatus currentStatus = repository.findCurrentStatus(correlationId)
			.orElseThrow(() -> new InvalidCorrelationIdException("Correlation id doesn't exist: " + correlationId.id));
		if(!currentStatus.isTransitionalTo(status))
			throw new IllegalStateTransitionException(String.format("Transition between %s and %s states is not allowed", currentStatus, status));

		if(status.equals(AccessStatus.REVOKED)) {
			ProjectUserGrant projectUserGrant = repository.findUsersGrantsByCorrelationId(correlationId)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Resource access correlation Id %s doesn't exist", correlationId)));
			repository.deleteByCorrelationId(correlationId);
			GrantAccess userGrant = GrantAccess.builder()
				.siteId(projectUserGrant.siteId)
				.projectId(projectUserGrant.projectId)
				.fenixUserId(projectUserGrant.userId)
				.build();
			userSiteAccessInnerService.revokeAccessToSite(userGrant);
			postCommitRunner.publishAfterCommit((new UserGrantRemovedEvent(userGrant)));
			LOG.info("UserAllocation with correlation id {} was removed", correlationId.id);
			return;
		}
		repository.update(correlationId, status, msg);
		LOG.info("UserAllocation status with correlation id {} was updated to {}", correlationId.id, status);
	}
}
