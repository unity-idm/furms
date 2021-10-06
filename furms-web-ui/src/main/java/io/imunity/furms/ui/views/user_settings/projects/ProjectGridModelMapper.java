/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import io.imunity.furms.api.applications.ProjectApplicationsService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.projects.Project;

import java.util.Set;

import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.ACTIVE;
import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.NOT_ACTIVE;
import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.REQUESTED;

class ProjectGridModelMapper {
	private final ProjectService projectService;
	private final ProjectApplicationsService projectApplicationsService;

	ProjectGridModelMapper(ProjectService projectService, ProjectApplicationsService projectApplicationsService) {
		this.projectService = projectService;
		this.projectApplicationsService = projectApplicationsService;
	}

	ProjectGridModel map(Project project){
		Set<String> projectsIds = projectApplicationsService.findAllAppliedProjectsIdsForCurrentUser();
		return ProjectGridModel.builder()
		.id(project.getId())
		.communityId(project.getCommunityId())
		.name(project.getName())
		.description(project.getDescription())
		.status(
			projectService.isUser(project.getId()) ? ACTIVE :
				projectsIds.contains(project.getId()) ? REQUESTED :
					NOT_ACTIVE
		)
		.build();
	}
}
