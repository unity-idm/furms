/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.project;

import org.springframework.stereotype.Component;

import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.User;

import java.util.List;

@Component
public class ProjectModelResolver {

	private final ProjectService projectService;
	private final UserService userService;

	ProjectModelResolver(ProjectService projectService, UserService userService) {
		this.projectService = projectService;
		this.userService = userService;
	}

	public ProjectViewModel resolve(String projectId) {
		Project project = projectService.findById(projectId)
				.orElseThrow(UnknownProjectException::new);
		return resolve(project);
	}
	
	public ProjectViewModel resolve(Project project) {
		User leader = userService.findById(project.getLeaderId())
				.orElse(null);
		return ProjectViewModelMapper.map(project, leader);
	}

	public ProjectViewModel resolve(List<User> users, Project project){
		User leader = users.stream()
			.filter(user -> user.id.equals(project.getLeaderId()))
			.findAny()
			.orElse(null);
		return ProjectViewModelMapper.map(project, leader);
	}

	public static class UnknownProjectException extends IllegalArgumentException {
		
	}
}
