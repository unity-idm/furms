/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.PersistentId;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.imunity.furms.domain.authz.roles.ResourceType.PROJECT;
import static io.imunity.furms.performance.tests.SecurityUserUtils.createSecurityUser;
import static java.util.stream.Collectors.toMap;

class ProjectMembershipsDataLoader {

	private final ProjectService projectService;

	ProjectMembershipsDataLoader(ProjectService projectService) {
		this.projectService = projectService;
	}

	Map<Data.User, Set<Data.Project>> loadProjectMemberships(Set<Data.User> users, List<Data.Project> projects) {
		final Map<Data.User, Set<Data.Project>> userProjects = new HashMap<>();
		createSecurityUser(projects.stream()
				.map(project -> project.projectId)
				.distinct()
				.collect(toMap(
						projectId -> new ResourceId(projectId, PROJECT),
						projectId -> Set.of(Role.PROJECT_ADMIN))));
		users.forEach(user -> {
			userProjects.put(user, new HashSet<>());
			addUserToProject(user, projects, userProjects);
			addUserToProject(user, projects, userProjects);
		});
		createSecurityUser(Map.of());

		return userProjects;
	}

	private void addUserToProject(Data.User user, List<Data.Project> projects, Map<Data.User, Set<Data.Project>> userProjects) {
		Data.Project project;
		do {
			final int index = new Random().ints(0, projects.size())
					.findFirst()
					.orElse(0);
			project = projects.get(index);
		} while (userProjects.get(user).contains(project));

		if (project == null) {
			throw new IllegalArgumentException("Project cannot be null");
		}
		projectService.addUser(project.communityId, project.projectId, new PersistentId(user.persistentId));

		userProjects.get(user).add(project);
	}
}
