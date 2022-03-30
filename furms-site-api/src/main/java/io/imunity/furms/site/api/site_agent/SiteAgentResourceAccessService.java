/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.resource_access.GrantAccess;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.users.FURMSUser;

public interface SiteAgentResourceAccessService {
	void grantAccess(CorrelationId correlationId, GrantAccess grantAccess, FURMSUser user);
	void revokeAccess(CorrelationId correlationId, GrantAccess grantAccess);
}
