/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;
import io.imunity.furms.site.api.message_resolver.UserOperationMessageResolver;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

@Service
class UserOperationMessageResolverImpl implements UserOperationMessageResolver {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UserOperationRepository repository;

	UserOperationMessageResolverImpl(UserOperationRepository repository) {
		this.repository = repository;
	}

	public void update(UserAddition userAddition){
		UserAdditionStatus status = repository.findAdditionStatusByCorrelationId(userAddition.correlationId.id);
		if(status.equals(UserAdditionStatus.ADDED) || status.equals(UserAdditionStatus.FAILED)){
			LOG.info("UserAddition with given correlation id {} cannot be modified", userAddition.correlationId.id);
			return;
		}
		repository.update(userAddition);
		LOG.info("UserAddition was update: {}", userAddition);
	}

	public void updateStatus(CorrelationId correlationId, UserRemovalStatus userRemovalStatus) {
		UserRemovalStatus status = repository.findRemovalStatusByCorrelationId(correlationId.id);
		if(status.equals(UserRemovalStatus.REMOVED) || status.equals(UserRemovalStatus.FAILED)){
			LOG.info("UserRemoval with given correlation id {} cannot be modified", correlationId.id);
			return;
		}
		repository.updateStatus(correlationId, userRemovalStatus);
		LOG.info("UserRemoval status with given correlation id {} was update to: {}", correlationId.id, userRemovalStatus);
	}

	public void updateStatus(CorrelationId correlationId, UserAdditionStatus userAdditionStatus) {
		UserAdditionStatus status = repository.findAdditionStatusByCorrelationId(correlationId.id);
		if(status.equals(UserAdditionStatus.ADDED) || status.equals(UserAdditionStatus.FAILED)){
			LOG.info("UserAddition with given correlation id {} cannot be modified", correlationId.id);
			return;
		}
		repository.updateStatus(correlationId, userAdditionStatus);
		LOG.info("UserAddition status with given correlation id {} was update to: {}", correlationId.id, userAdditionStatus);
	}

}
