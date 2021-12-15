/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import org.springframework.stereotype.Service;

import static io.imunity.furms.domain.authz.roles.Capability.*;
import static io.imunity.furms.domain.authz.roles.ResourceType.*;

@Service
@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
public class ServiceMock{

	@FurmsAuthorize(capability = USERS_MAINTENANCE, id = "id")
	public void findById(String id) {
	}

	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT)
	public void findAll() {
	}

	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "id")
	public void getCommunity(String id) {
	}

	@FurmsAuthorize(capability = PROJECT_READ, resourceType = PROJECT, id = "id")
	public void getProject(String id) {
	}

	@FurmsAuthorize(capability = PROJECT_LIMITED_READ, resourceType = PROJECT, id = "id")
	public void getLimitedProject(String id) {
	}

	public void findAllWithClassScopeAuthorization() {
	}
}
