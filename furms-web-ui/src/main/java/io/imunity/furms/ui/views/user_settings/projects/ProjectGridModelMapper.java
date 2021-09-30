/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import io.imunity.furms.api.applications.ApplicationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.projects.Project;

import java.util.Set;

import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.ACTIVE;
import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.NOT_ACTIVE;
import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.REQUESTED;

class ProjectGridModelMapper {
	private final ProjectService projectService;
	private final ApplicationService applicationService;

	ProjectGridModelMapper(ProjectService projectService, ApplicationService applicationService) {
		this.projectService = projectService;
		this.applicationService = applicationService;
	}

	ProjectGridModel map(Project project){
		Set<String> projectsIds = applicationService.findAllAppliedProjectsIdsForCurrentUser();
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
