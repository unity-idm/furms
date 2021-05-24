/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_access;

import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Set;

public interface ResourceAccessRepository {
	Set<UserGrant> findUsersGrants(String projectId);
	void create(CorrelationId correlationId, GrantAccess grantAccess);
	void update(CorrelationId correlationId, GrantAccess grantAccess, AccessStatus status);
	void update(CorrelationId correlationId, AccessStatus status, String msg);
	boolean exists(GrantAccess grantAccess);
	AccessStatus findCurrentStatus(FenixUserId userId, String allocationId);
	AccessStatus findCurrentStatus(CorrelationId correlationId);
	void deleteByCorrelationId(CorrelationId correlationId);
	void deleteByUserAndAllocationId(FenixUserId userId, String allocationId);
	void deleteByUserAndProjectId(FenixUserId userId, String projectId);
	void deleteAll();
}
