/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.performance.tests.data;

import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.spi.projects.ProjectGroupsDAO;

import java.util.List;
import java.util.Random;
import java.util.Set;

class ProjectMembershipsDataLoader {

	private final ProjectGroupsDAO projectGroupsDAO;

	ProjectMembershipsDataLoader(ProjectGroupsDAO projectGroupsDAO) {
		this.projectGroupsDAO = projectGroupsDAO;
	}

	void loadProjectMemberships(Set<Data.User> users, List<Data.Project> projects) {
		Random random = new Random();
		int size = projects.size();

		users.stream()
			.parallel()
			.forEach(user -> {
				Data.Project project = projects.get(random.nextInt(size));
				projectGroupsDAO.addProjectUser(
					project.communityId,
					project.projectId,
					new PersistentId(user.persistentId),
					Role.PROJECT_USER
				);
				project = projects.get(random.nextInt(size));
				projectGroupsDAO.addProjectUser(
					project.communityId,
					project.projectId,
					new PersistentId(user.persistentId),
					Role.PROJECT_USER
				);
			});
	}
}
