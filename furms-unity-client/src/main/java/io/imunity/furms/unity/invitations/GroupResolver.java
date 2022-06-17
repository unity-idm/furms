/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.unity.invitations;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.projects.ProjectRepository;
import org.springframework.stereotype.Component;

import static io.imunity.furms.unity.common.UnityConst.COMMUNITY_PREFIX;
import static io.imunity.furms.unity.common.UnityConst.FENIX_PATTERN;
import static io.imunity.furms.unity.common.UnityConst.PROJECT_PREFIX;
import static io.imunity.furms.unity.common.UnityConst.SITE_PREFIX;
import static io.imunity.furms.unity.common.UnityPaths.USERS_PATTERN;

@Component
class GroupResolver {
	private final ProjectRepository projectRepository;

	GroupResolver(ProjectRepository projectRepository) {
		this.projectRepository = projectRepository;
	}

	String resolveGroup(ResourceId resourceId, Role role){
		switch (role){
			case FENIX_ADMIN :
				return FENIX_PATTERN;
			case SITE_ADMIN:
			case SITE_SUPPORT:
				return SITE_PREFIX + resourceId.asSiteId().id + USERS_PATTERN;
			case COMMUNITY_ADMIN:
				return COMMUNITY_PREFIX + resourceId.asCommunityId().id + USERS_PATTERN;
			case PROJECT_ADMIN:
			case PROJECT_USER:
				Project project = projectRepository.findById(resourceId.asProjectId()).get();
				return COMMUNITY_PREFIX + project.getCommunityId().id + PROJECT_PREFIX + project.getId().id + USERS_PATTERN;
			default:
				throw new IllegalArgumentException("This shouldn't happen, invitation always need role");
		}
	}
}
