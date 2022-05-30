/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.sites.SiteId;
import org.springframework.stereotype.Service;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.*;

@Service
@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
public class ServiceMock{

	@FurmsAuthorize(capability = USERS_MAINTENANCE, id = "id")
	public void findById(SiteId id) {
	}

	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
	public void findAll() {
	}

	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "id")
	public void getCommunity(CommunityId id) {
	}

	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "id")
	public void getProject(ProjectId id) {
	}

	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "id")
	public void getLimitedProject(ProjectId id) {
	}

	public void findAllWithClassScopeAuthorization() {
	}
}
