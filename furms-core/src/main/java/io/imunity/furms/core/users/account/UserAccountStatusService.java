/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.core.users.account;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.springframework.stereotype.Service;

@Service
class UserAccountStatusService {

	private final UserOperationRepository repository;

	UserAccountStatusService(UserOperationRepository repository) {
		this.repository = repository;
	}

	UserAddition findByCorrelationId(CorrelationId correlationId) {
		return repository.findAdditionByCorrelationId(correlationId);
	}
}
