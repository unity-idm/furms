/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.site.api.message_remover.UserProjectAddRemover;
import io.imunity.furms.spi.user_operation.UserOperationRepository;
import org.springframework.stereotype.Component;

@Component
class PendingUserInstallationRemover implements UserProjectAddRemover {
	private final UserOperationRepository userOperationRepository;

	PendingUserInstallationRemover(UserOperationRepository userOperationRepository) {
		this.userOperationRepository = userOperationRepository;
	}

	@Override
	public void remove(CorrelationId correlationId) {
		userOperationRepository.deleteByCorrelationId(correlationId);
	}
}
