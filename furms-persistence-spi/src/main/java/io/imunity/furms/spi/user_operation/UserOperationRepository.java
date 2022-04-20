/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.user_operation;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.user_operation.UserAdditionErrorMessage;
import io.imunity.furms.domain.user_operation.UserAdditionJob;
import io.imunity.furms.domain.user_operation.UserAdditionWithProject;
import io.imunity.furms.domain.user_operation.UserStatus;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;

public interface UserOperationRepository {
	Set<String> findUserIds(ProjectId projectId);
	Set<UserAddition> findAllUserAdditions(ProjectId projectId, FenixUserId userId);
	Optional<UserAddition> findUserAddition(SiteId siteId, ProjectId projectId, FenixUserId userId);
	Set<UserAddition> findAllUserAdditions(ProjectId projectId);
	Set<UserAddition> findAllUserAdditions(FenixUserId userId);
	Set<UserAddition> findAllUserAdditions(SiteId siteId, FenixUserId userId);
	Set<UserAddition> findAllUserAdditionsBySiteId(SiteId siteId);
	Set<UserAddition> findAllUserAdditionsByProjectId(ProjectId projectId);
	Set<UserAdditionWithProject> findAllUserAdditionsWithSiteAndProjectBySiteId(FenixUserId userId, SiteId siteId);
	String create(UserAddition userAddition);
	void update(UserAddition userAddition);
	void update(UserAdditionJob userAdditionJob);
	void updateStatus(CorrelationId correlationId, UserStatus userStatus, Optional<UserAdditionErrorMessage> userErrorMessage);
	boolean isUserAdded(SiteId siteId, FenixUserId userId);
	Optional<UserStatus> findAdditionStatusByCorrelationId(CorrelationId correlationId);
	Optional<UserStatus> findAdditionStatus(SiteId siteId, ProjectId projectId, FenixUserId userId);
	UserAddition findAdditionByCorrelationId(CorrelationId correlationId);
	void deleteByCorrelationId(CorrelationId correlationId);
	boolean existsByUserIdAndSiteIdAndProjectId(FenixUserId userId, SiteId siteId, ProjectId projectId);
	boolean isUserInstalledOnSite(FenixUserId userId, SiteId siteId);
	void deleteAll();
	void delete(UserAddition userAddition);
}
