/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.admin;

import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.resource_access.UsersWithProjectAccess;
import io.imunity.furms.domain.sites.SiteInstalledProject;
import io.imunity.furms.domain.sites.SiteInstalledProjectResolved;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.rest.error.exceptions.FenixIdNotFoundException;
import io.imunity.furms.rest.user.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
class ProjectsRestConverter {

	private final ProjectInstallationsService projectInstallationsService;
	private final ResourceAccessService resourceAccessService;
	private final UserService userService;
	private final ProjectService projectService;

	ProjectsRestConverter(ProjectInstallationsService projectInstallationsService,
	                      ResourceAccessService resourceAccessService,
	                      UserService userService,
	                      ProjectService projectService) {
		this.projectInstallationsService = projectInstallationsService;
		this.resourceAccessService = resourceAccessService;
		this.userService = userService;
		this.projectService = projectService;
	}

	public ProjectWithUsers convertToProjectWithUsers(io.imunity.furms.domain.projects.Project project) {
		return new ProjectWithUsers(
				convert(project),
				new ArrayList<>(resourceAccessService.findAddedUser(project.getId())));
	}

	public ProjectWithUsers convertToProjectWithUsers(SiteInstalledProjectResolved siteInstalledProject) {
		final List<String> userIds = resourceAccessService.findAddedUserBySiteId(siteInstalledProject.siteId).stream()
				.filter(project -> project.getProjectId().equals(siteInstalledProject.project.getId()))
				.findFirst()
				.map(UsersWithProjectAccess::getUserIds)
				.orElse(List.of());
		return new ProjectWithUsers(convert(siteInstalledProject.project), userIds);
	}

	public Project convert(io.imunity.furms.domain.projects.Project project) {
		final Set<SiteInstalledProject> projectInstallations =
				projectInstallationsService.findAllSiteInstalledProjectsByProjectId(project.getId());
		final User user = findLeader(project);
		return new Project(project, user, projectInstallations);
	}

	public PersistentId convertToPersistentId(FenixUserId fenixUserId) {
		return userService.findByFenixUserId(fenixUserId)
				.flatMap(user -> user.id)
				.orElseThrow(() -> new FenixIdNotFoundException("User with provided fenixId doesn't exist."));
	}

	private User findLeader(io.imunity.furms.domain.projects.Project project) {
		try {
			return userService.findById(project.getLeaderId())
					.map(User::new)
					.orElse(null);
		} catch (AccessDeniedException e) {
			return projectService.findProjectLeaderInfoAsInstalledUser(project.getId())
					.map(User::new)
					.orElse(null);

		}
	}
}
