/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_access;

import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.ProjectUserGrant;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ResourceAccessRepository {
	Optional<ProjectUserGrant> findUsersGrantsByCorrelationId(CorrelationId correlationId);
	Set<FenixUserId> findUsersBySiteId(String siteId);
	Set<UserGrant> findUsersGrantsByProjectId(String projectId);
	Set<UserGrant> findUserGrantsByProjectIdAndFenixUserId(String projectId, FenixUserId fenixUserId);
	UUID create(CorrelationId correlationId, GrantAccess grantAccess, AccessStatus status);
	void update(CorrelationId correlationId, GrantAccess grantAccess, AccessStatus status);
	void update(CorrelationId correlationId, AccessStatus status, String msg);
	boolean exists(GrantAccess grantAccess);
	AccessStatus findCurrentStatus(FenixUserId userId, String allocationId);
	Set<GrantAccess> findWaitingGrantAccesses(FenixUserId userId, String projectId, String siteId);
	AccessStatus findCurrentStatus(CorrelationId correlationId);
	String findSiteIdByCorrelationId(CorrelationId correlationId);
	void deleteByCorrelationId(CorrelationId correlationId);
	void deleteByUserAndAllocationId(FenixUserId userId, String allocationId);
	void deleteByUserAndProjectId(FenixUserId userId, String projectId);
	void deleteByUserAndSiteIdAndProjectId(FenixUserId userId, String siteId, String projectId);
	void deleteAll();
}
