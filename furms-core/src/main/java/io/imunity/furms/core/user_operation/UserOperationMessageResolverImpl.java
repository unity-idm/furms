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
import org.springframework.stereotype.Service;

@Service
class UserOperationMessageResolverImpl implements UserOperationMessageResolver {
	private final UserOperationRepository repository;

	UserOperationMessageResolverImpl(UserOperationRepository repository) {
		this.repository = repository;
	}

	public void update(UserAddition userAddition){
		repository.update(userAddition);
	}

	public void updateStatus(CorrelationId correlationId, UserRemovalStatus userRemovalStatus) {
		repository.updateStatus(correlationId, userRemovalStatus);

	}

	public void updateStatus(CorrelationId correlationId, UserAdditionStatus userAdditionStatus) {
		repository.updateStatus(correlationId, userAdditionStatus);
	}

}
