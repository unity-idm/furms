/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.core.post_commit.PostCommitRunner;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.site_agent.IllegalStateTransitionException;
import io.imunity.furms.domain.site_agent.InvalidCorrelationIdException;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionErrorMessage;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.site_agent.SiteAgentResourceAccessService;
import io.imunity.furms.site.api.status_updater.UserOperationStatusUpdater;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import io.imunity.furms.spi.users.UsersDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Set;

@Service
class UserOperationStatusUpdaterImpl implements UserOperationStatusUpdater {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteAgentResourceAccessService siteAgentResourceAccessService;
	private final UserOperationRepository repository;
	private final ResourceAccessRepository resourceAccessRepository;
	private final UsersDAO usersDAO;
	private final PostCommitRunner postCommitRunner;

	UserOperationStatusUpdaterImpl(SiteAgentResourceAccessService siteAgentResourceAccessService,
	                               UserOperationRepository repository,
	                               ResourceAccessRepository resourceAccessRepository,
	                               UsersDAO usersDAO,
	                               PostCommitRunner postCommitRunner) {
		this.siteAgentResourceAccessService = siteAgentResourceAccessService;
		this.repository = repository;
		this.resourceAccessRepository = resourceAccessRepository;
		this.usersDAO = usersDAO;
		this.postCommitRunner = postCommitRunner;
	}

	@Transactional
	public void update(UserAddition userAddition){
		UserStatus status = repository.findAdditionStatusByCorrelationId(userAddition.correlationId)
			.orElseThrow(() -> new InvalidCorrelationIdException("Correlation Id not found"));
		if(!status.isTransitionalTo(userAddition.status)){
			throw new IllegalStateTransitionException(String.format("Transition between %s and %s states is not allowed, UserAddition is %s", status, userAddition.status, status));
		}
		repository.update(userAddition);
		if(userAddition.status.equals(UserStatus.ADDED)){
			UserAddition addition = repository.findAdditionByCorrelationId(userAddition.correlationId);
			sendQueuedGrandAccess(addition.siteId, addition.projectId, addition.userId);
		}

		LOG.info("UserAddition was correlation id {} was added", userAddition.correlationId.id);
	}

	private void sendQueuedGrandAccess(SiteId siteId, ProjectId projectId, FenixUserId userId) {
		Set<GrantAccess> userGrants = resourceAccessRepository.findWaitingGrantAccesses(userId, projectId, siteId);
		Optional<FURMSUser> furmsUser = usersDAO.findById(userId);
		for (GrantAccess grantAccess : userGrants) {
			CorrelationId correlationId = CorrelationId.randomID();
			resourceAccessRepository.update(correlationId, grantAccess, AccessStatus.GRANT_PENDING);
			postCommitRunner.runAfterCommit(() -> siteAgentResourceAccessService.grantAccess(correlationId,
				grantAccess, furmsUser.get()));
			LOG.info("UserAllocation with correlation id {} was created {}", correlationId.id, grantAccess);
		}
	}

	@Transactional
	public void updateStatus(CorrelationId correlationId, UserStatus userStatus, Optional<UserAdditionErrorMessage> userErrorMessage) {
		UserStatus status = repository.findAdditionStatusByCorrelationId(correlationId)
			.orElseThrow(() -> new InvalidCorrelationIdException("Correlation Id not found"));
		if(!status.isTransitionalTo(userStatus)){
			throw new IllegalStateTransitionException(String.format("Transition between %s and %s states is not allowed", status, userStatus));
		}

		if(userStatus.equals(UserStatus.REMOVED)){
			UserAddition userAddition = repository.findAdditionByCorrelationId(correlationId);
			repository.deleteByCorrelationId(correlationId);
			LOG.info("UserAddition with given correlation id {} was deleted", correlationId.id);
			resourceAccessRepository.deleteByUserAndSiteIdAndProjectId(userAddition.userId, userAddition.siteId, userAddition.projectId);
			LOG.info("User {} grants in project {} were deleted", userAddition.userId, userAddition.projectId);
			return;
		}
		repository.updateStatus(correlationId, userStatus, userErrorMessage);
		LOG.info("UserAddition status with given correlation id {} was updated: {}", correlationId.id, userStatus);
	}

}
