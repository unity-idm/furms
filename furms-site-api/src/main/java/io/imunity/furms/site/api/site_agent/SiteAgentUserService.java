/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteExternalId;
import io.imunity.furms.domain.user_addition.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;

import java.util.List;

public interface SiteAgentUserService {
	void addUser(CorrelationId correlationId, List<SiteExternalId> ids, FURMSUser user, String projectId);
	void removeUser(CorrelationId correlationId, List<UserAddition> userAdditions);
}
