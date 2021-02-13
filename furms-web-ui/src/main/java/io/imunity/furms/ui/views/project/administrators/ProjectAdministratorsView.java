/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.administrators;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

import com.vaadin.flow.router.Route;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.AdministratorsGridComponent;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

@Route(value = "project/admin/administrators", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.administrators.page.title")
public class ProjectAdministratorsView extends FurmsViewComponent {
	public ProjectAdministratorsView(ProjectService projectService, AuthzService authzService, UserService userService) {
		String currentUserId = authzService.getCurrentUserId();
		String projectId = getCurrentResourceId();
		Project project = projectService.findById(projectId).get();

		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> projectService.findAllAdmins(project.getCommunityId(), project.getId())
		);
		AdministratorsGridComponent grid = new AdministratorsGridComponent(
			() -> projectService.findAllAdmins(project.getCommunityId(), project.getId()),
			userId -> {
				projectService.removeAdmin(project.getCommunityId(), project.getId(), userId);
				inviteUser.reload();
			},
			currentUserId,
			true
		);
		inviteUser.addInviteAction(event -> {
			projectService.inviteAdmin(project.getCommunityId(), project.getId(), inviteUser.getEmail());
			grid.reloadGrid();
			inviteUser.reload();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.project-admin.administrators.page.header"));
		getContent().add(headerLayout, inviteUser, grid);
	}
}
