/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.user_operation.UserAdditionJob;

import java.util.Set;

public interface UserOperationRepository {
	Set<UserAddition> findAllUserAdditions(String projectId, String userId);
	String create(UserAddition userAddition);
	void update(UserAddition userAddition);
	void update(UserAdditionJob userAdditionJob);
	void updateStatus(CorrelationId correlationId, UserStatus userStatus, String message);
	boolean isUserAdded(String siteId, String userId);
	Set<String> findAddedUserIds(String projectId);
	UserStatus findAdditionStatusByCorrelationId(String correlationId);
	void deleteByCorrelationId(String correlationId);
}
