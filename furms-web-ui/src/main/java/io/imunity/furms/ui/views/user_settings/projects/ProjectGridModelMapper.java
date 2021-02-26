/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.projects.Project;

import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.ACTIVE;
import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.NOT_ACTIVE;

class ProjectGridModelMapper {
	private final ProjectService projectService;

	public ProjectGridModelMapper(ProjectService projectService) {
		this.projectService = projectService;
	}

	ProjectGridModel map(Project project){
		return ProjectGridModel.builder()
		.id(project.getId())
		.communityId(project.getCommunityId())
		.name(project.getName())
		.description(project.getDescription())
		.status(projectService.isUser(project.getId()) ? ACTIVE : NOT_ACTIVE)
		.build();
	}
}
