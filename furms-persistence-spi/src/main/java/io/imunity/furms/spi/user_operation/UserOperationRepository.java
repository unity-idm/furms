/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionStatus;
import io.imunity.furms.domain.user_operation.UserRemoval;
import io.imunity.furms.domain.user_operation.UserRemovalStatus;

import java.util.Set;

public interface UserOperationRepository {
	Set<UserAddition> findAllUserAdditions(String projectId, String userId);
	String create(UserAddition userAddition);
	String create(UserRemoval userRemoval);
	void update(UserAddition userAddition);
	void updateStatus(CorrelationId correlationId, UserRemovalStatus userRemovalStatus);
	void updateStatus(CorrelationId correlationId, UserAdditionStatus userAdditionStatus);
	boolean isUserAdded(String siteId, String userId);
}
