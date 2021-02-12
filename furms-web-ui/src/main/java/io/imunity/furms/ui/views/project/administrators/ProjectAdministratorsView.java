/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.administrators;

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

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "project/admin/administrators", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.administrators.page.title")
public class ProjectAdministratorsView extends FurmsViewComponent {
	public ProjectAdministratorsView(ProjectService projectService, AuthzService authzService, UserService userService) {
		String currentUserId = authzService.getCurrentUserId();
		String projectId = getCurrentResourceId();
		Project project = projectService.findById(projectId).get();

		AdministratorsGridComponent grid = new AdministratorsGridComponent(
			() -> projectService.findAllAdmins(project.getCommunityId(), project.getId()),
			userId -> projectService.removeAdmin(project.getCommunityId(), project.getId(), userId),
			currentUserId,
			true
		);
		InviteUserComponent inviteUser = new InviteUserComponent(userService.getAllUsers());
		inviteUser.addInviteAction(event -> {
			projectService.inviteAdmin(project.getCommunityId(), project.getId(), inviteUser.getEmail());
			grid.reloadGrid();
			inviteUser.clear();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.project-admin.administrators.page.header"),
			inviteUser
		);
		getContent().add(headerLayout, grid);
	}
}
