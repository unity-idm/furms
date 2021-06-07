/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionErrorMessage;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.site.api.status_updater.UserOperationStatusUpdater;
import io.imunity.furms.spi.resource_access.ResourceAccessRepository;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

@Service
class UserOperationStatusUpdaterImpl implements UserOperationStatusUpdater {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UserOperationRepository repository;
	private final ResourceAccessRepository resourceAccessRepository;

	UserOperationStatusUpdaterImpl(UserOperationRepository repository, ResourceAccessRepository resourceAccessRepository) {
		this.repository = repository;
		this.resourceAccessRepository = resourceAccessRepository;
	}

	@Transactional
	public void update(UserAddition userAddition){
		UserStatus status = repository.findAdditionStatusByCorrelationId(userAddition.correlationId.id);
		if(!status.isTransitionalTo(userAddition.status)){
			throw new IllegalArgumentException(String.format("Transition between %s and %s states is not allowed, UserAddition is %s", status, userAddition.status, status));
		}
		repository.update(userAddition);
		LOG.info("UserAddition was correlation id {} was added", userAddition.correlationId.id);
	}

	@Transactional
	public void updateStatus(CorrelationId correlationId, UserStatus userStatus, Optional<UserAdditionErrorMessage> userErrorMessage) {
		UserStatus status = repository.findAdditionStatusByCorrelationId(correlationId.id);
		if(!status.isTransitionalTo(userStatus)){
			throw new IllegalArgumentException(String.format("Transition between %s and %s states is not allowed", status, userStatus));
		}
		if(userStatus.equals(UserStatus.REMOVED)){
			UserAddition userAddition = repository.findAdditionByCorrelationId(correlationId);
			repository.deleteByCorrelationId(correlationId.id);
			LOG.info("UserAddition with given correlation id {} was deleted", correlationId.id);
			resourceAccessRepository.deleteByUserAndProjectId(new FenixUserId(userAddition.userId), userAddition.projectId);
			LOG.info("User {} grants in project {} were deleted", userAddition.userId, userAddition.projectId);
			return;
		}
		repository.updateStatus(correlationId, userStatus, userErrorMessage);
		LOG.info("UserAddition status with given correlation id {} was updated: {}", correlationId.id, userStatus);
	}

}
