/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.core.config.security.method;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.core.config.security.user.capability.Capability.*;
import static io.imunity.furms.core.config.security.user.resource.ResourceType.APP_LEVEL;
import static io.imunity.furms.core.config.security.user.resource.ResourceType.COMMUNITY;

@Service
@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
public class ServiceMock{

	@FurmsAuthorize(capability = AUTHENTICATED, resourceType = APP_LEVEL, id = "id")
	public Optional<Object> findById(String id) {
		return Optional.empty();
	}

	@FurmsAuthorize(capability = FENIX_ADMINS_MANAGEMENT, resourceType = APP_LEVEL)
	public Set<Object> findAll() {
		return Collections.emptySet();
	}

	@FurmsAuthorize(capability = COMMUNITY_READ, resourceType = COMMUNITY, id = "id")
	public Optional<Object> getCommunity(String id) {
		return Optional.empty();
	}

	public Set<Object> findAllWithClassScopeAuthorization() {
		return Collections.emptySet();
	}
}
