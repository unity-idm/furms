/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.spi.resource_access;

import io.imunity.furms.domain.resource_access.AccessStatus;
import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Set;

public interface ResourceAccessRepository {
	Set<UserGrant> findUsersGrants(String projectId);
	void create(CorrelationId correlationId, GrantAccess grantAccess);
	void update(CorrelationId correlationId, GrantAccess grantAccess);
	void update(CorrelationId correlationId, AccessStatus status, String msg);
	void delete(CorrelationId correlationId);
	void deleteAll();
}
