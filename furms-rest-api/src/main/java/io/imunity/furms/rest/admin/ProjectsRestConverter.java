/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.rest.error.exceptions.FenixIdNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;

@Component
class ProjectsRestConverter {

	private final ProjectInstallationsService projectInstallationsService;
	private final ResourceAccessService resourceAccessService;
	private final UserService userService;

	ProjectsRestConverter(ProjectInstallationsService projectInstallationsService,
	                      ResourceAccessService resourceAccessService,
	                      UserService userService) {
		this.projectInstallationsService = projectInstallationsService;
		this.resourceAccessService = resourceAccessService;
		this.userService = userService;
	}

	public ProjectWithUsers convertToProjectWithUsers(io.imunity.furms.domain.projects.Project project) {
		return new ProjectWithUsers(
				convert(project),
				new ArrayList<>(resourceAccessService.findAddedUser(project.getId())));
	}

	public Project convert(io.imunity.furms.domain.projects.Project project) {
		final Set<SiteInstalledProject> projectInstallations =
				projectInstallationsService.findAllSiteInstallionProjectsByProjectId(project.getId());
		final User user = findUser(project.getLeaderId());
		return new Project(project, user, projectInstallations);
	}

	public PersistentId convertToPersistentId(FenixUserId fenixUserId) {
		return userService.findByFenixUserId(fenixUserId)
				.flatMap(user -> user.id)
				.orElseThrow(() -> new FenixIdNotFoundException("User with provided fenixId doesn't exist."));
	}

	private User findUser(PersistentId userId) {
		return userService.findById(userId)
				.map(User::new)
				.orElse(null);
	}

}
