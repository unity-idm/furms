/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.status_updater;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionErrorMessage;
import io.imunity.furms.domain.user_operation.UserStatus;

import java.util.Optional;

public interface UserOperationStatusUpdater {
	void update(UserAddition userAddition);
	void updateStatus(CorrelationId correlationId, UserStatus userStatus, Optional<UserAdditionErrorMessage> userErrorMessage);
}
