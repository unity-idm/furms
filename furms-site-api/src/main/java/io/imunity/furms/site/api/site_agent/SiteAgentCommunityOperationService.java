/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.site.api.site_agent;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.communities.CommunityInstallation;
import io.imunity.furms.domain.communities.CommunityUpdate;

public interface SiteAgentCommunityOperationService
{
	void installCommunity(CommunityInstallation community);
	void updateCommunity(CommunityUpdate community);

	void removeCommunity(CommunityId id);
}
