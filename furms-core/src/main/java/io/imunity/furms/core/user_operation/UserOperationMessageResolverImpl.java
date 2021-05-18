/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.site.api.message_resolver.UserOperationMessageResolver;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.domain.resource_access.AccessStatus.REVOKE_PENDING;

@Service
class UserOperationMessageResolverImpl implements UserOperationMessageResolver {
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UserOperationRepository repository;

	UserOperationMessageResolverImpl(UserOperationRepository repository) {
		this.repository = repository;
	}

	public void update(UserAddition userAddition){
		UserStatus status = repository.findAdditionStatusByCorrelationId(userAddition.correlationId.id);
		if(!status.isTransitionalTo(userAddition.status)){
			throw new IllegalArgumentException(String.format("Transition between %s and %s states is not allowed", status, userAddition.status));
		}
		repository.update(userAddition);
		LOG.info("UserAddition was correlation id {} was added", userAddition.correlationId.id);
	}

	public void updateStatus(CorrelationId correlationId, UserStatus userStatus, String message) {
		UserStatus status = repository.findAdditionStatusByCorrelationId(correlationId.id);
		if(!status.isTransitionalTo(userStatus)){
			throw new IllegalArgumentException(String.format("Transition between %s and %s states is not allowed", status, userStatus));
		}
		if(userStatus.equals(UserStatus.REMOVED)){
			repository.deleteByCorrelationId(correlationId.id);
			LOG.info("UserAddition with given correlation id {} was deleted", correlationId.id);
			return;
		}
		repository.updateStatus(correlationId, userStatus, message);
		LOG.info("UserAddition status with given correlation id {} was update to: {}", correlationId.id, userStatus);
	}

}
