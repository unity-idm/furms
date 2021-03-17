/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.project;

import io.imunity.furms.domain.users.FURMSUser;
import org.springframework.stereotype.Component;

import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.projects.Project;

import java.time.ZoneId;
import java.util.List;

@Component
public class ProjectModelResolver {

	private final ProjectService projectService;
	private final UserService userService;

	ProjectModelResolver(ProjectService projectService, UserService userService) {
		this.projectService = projectService;
		this.userService = userService;
	}

	public ProjectViewModel resolve(String projectId, ZoneId zoneId) {
		Project project = projectService.findById(projectId)
				.orElseThrow(UnknownProjectException::new);
		return resolve(project, zoneId);
	}
	
	public ProjectViewModel resolve(Project project, ZoneId zoneId) {
		FURMSUser leader = userService.findById(project.getLeaderId())
				.orElse(null);
		return ProjectViewModelMapper.map(project, leader, zoneId);
	}

	public ProjectViewModel resolve(List<FURMSUser> users, Project project, ZoneId zoneId){
		FURMSUser leader = users.stream()
			.filter(user -> user.id.equals(project.getLeaderId()))
			.findAny()
			.orElse(null);
		return ProjectViewModelMapper.map(project, leader, zoneId);
	}

	public static class UnknownProjectException extends IllegalArgumentException {
		
	}
}
