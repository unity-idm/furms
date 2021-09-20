/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.user_operation;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionErrorMessage;
import io.imunity.furms.domain.user_operation.UserAdditionJob;
import io.imunity.furms.domain.user_operation.UserAdditionWithProject;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;

public interface UserOperationRepository {
	Set<String> findUserIds(String projectId);
	Set<UserAddition> findAllUserAdditions(String projectId, String userId);
	Set<UserAddition> findAllUserAdditions(String userId);
	Set<UserAddition> findAllUserAdditionsByUserId(String siteId);
	Set<UserAdditionWithProject> findAllUserAdditionsWithSiteAndProjectBySiteId(String userId, String siteId);
	String create(UserAddition userAddition);
	void update(UserAddition userAddition);
	void update(UserAdditionJob userAdditionJob);
	void updateStatus(CorrelationId correlationId, UserStatus userStatus, Optional<UserAdditionErrorMessage> userErrorMessage);
	boolean isUserAdded(String siteId, String userId);
	UserStatus findAdditionStatusByCorrelationId(String correlationId);
	Optional<UserStatus> findAdditionStatus(String siteId, String projectId, FenixUserId userId);
	UserAddition findAdditionByCorrelationId(CorrelationId correlationId);
	String findSiteIdByCorrelationId(CorrelationId correlationId);
	void deleteByCorrelationId(String correlationId);
	boolean existsByUserIdAndProjectId(FenixUserId userId, String projectId);
	void deleteAll();
	void delete(UserAddition userAddition);
}
