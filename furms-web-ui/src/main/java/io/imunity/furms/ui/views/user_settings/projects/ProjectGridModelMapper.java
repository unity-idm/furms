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
	private final String  id;
	private final ProjectService projectService;

	public ProjectGridModelMapper(String id, ProjectService projectService) {
		this.id = id;
		this.projectService = projectService;
	}

	//FIXME that is causing a REST query to Unity for each project in the system in case user enters settings page.
	// instead all user's projects should be retrieved first.
	ProjectGridModel map(Project project){
		return ProjectGridModel.builder()
		.id(project.getId())
		.communityId(project.getCommunityId())
		.name(project.getName())
		.description(project.getDescription())
		.status(projectService.isUser(project.getCommunityId(), project.getId(), id) ? ACTIVE : NOT_ACTIVE)
		.build();
	}
}
