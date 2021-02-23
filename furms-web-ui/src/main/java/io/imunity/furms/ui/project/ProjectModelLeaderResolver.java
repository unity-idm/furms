/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.project;

import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.User;

import java.util.List;

public class ProjectModelLeaderResolver {
	private final List<User> users;

	public ProjectModelLeaderResolver(List<User> users) {
		this.users = users;
	}

	public ProjectViewModel resolve(Project project){
		User leader = users.stream()
			.filter(user -> user.id.equals(project.getLeaderId()))
			.findAny()
			.orElse(null);
		return ProjectViewModelMapper.map(project, leader);
	}
}
