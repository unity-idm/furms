/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.message_resolver;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;

public interface UserOperationMessageResolver {
	void update(UserAddition userAddition);
	void updateStatus(CorrelationId correlationId, UserStatus userStatus, String message);
}
