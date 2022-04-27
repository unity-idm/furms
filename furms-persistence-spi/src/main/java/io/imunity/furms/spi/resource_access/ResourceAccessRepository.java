/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_access;

import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.GrantId;
import io.imunity.furms.domain.resource_access.ProjectUserGrant;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Optional;
import java.util.Set;

public interface ResourceAccessRepository {
	Optional<ProjectUserGrant> findUsersGrantsByCorrelationId(CorrelationId correlationId);
	Set<FenixUserId> findUsersBySiteId(SiteId siteId);
	Set<UserGrant> findUsersGrantsByProjectId(ProjectId projectId);
	Set<UserGrant> findUserGrantsByProjectIdAndFenixUserId(ProjectId projectId, FenixUserId fenixUserId);
	boolean existsBySiteIdAndProjectIdAndFenixUserId(SiteId siteId, ProjectId projectId, FenixUserId fenixUserId);
	GrantId create(CorrelationId correlationId, GrantAccess grantAccess, AccessStatus status);
	void update(CorrelationId correlationId, GrantAccess grantAccess, AccessStatus status);
	void update(CorrelationId correlationId, AccessStatus status, String msg);
	boolean exists(GrantAccess grantAccess);
	AccessStatus findCurrentStatus(FenixUserId userId, ProjectAllocationId allocationId);
	Set<GrantAccess> findWaitingGrantAccesses(FenixUserId userId, ProjectId projectId, SiteId siteId);
	Set<GrantAccess> findGrantAccessesBy(SiteId siteId, ProjectAllocationId projectAllocationId);
	Optional<AccessStatus> findCurrentStatus(CorrelationId correlationId);
	void deleteByCorrelationId(CorrelationId correlationId);
	void deleteByUserAndAllocationId(FenixUserId userId, ProjectAllocationId allocationId);
	void deleteByUserAndProjectId(FenixUserId userId, ProjectId projectId);
	void deleteByUserAndSiteIdAndProjectId(FenixUserId userId, SiteId siteId, ProjectId projectId);
	void deleteAll();
}
