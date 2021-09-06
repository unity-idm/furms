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
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.util.Set;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "project/admin/administrators", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.administrators.page.title")
public class ProjectAdministratorsView extends FurmsViewComponent {
	public ProjectAdministratorsView(ProjectService projectService, AuthzService authzService, UserService userService) {
		PersistentId currentUserId = authzService.getCurrentUserId();
		String projectId = getCurrentResourceId();
		Project project = projectService.findById(projectId)
				.orElseThrow(() -> new IllegalStateException("Project not found: " + projectId));

		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> projectService.findAllAdmins(project.getCommunityId(), project.getId())
		);
		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.redirectOnCurrentUserRemoval()
			.withRemoveUserAction(userId -> {
				projectService.removeAdmin(project.getCommunityId(), project.getId(), userId);
				inviteUser.reload();
			}).build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		UsersGridComponent grid = UsersGridComponent.defaultInit(() -> projectService.findAllAdmins(project.getCommunityId(), project.getId()), Set::of, userGrid);

		inviteUser.addInviteAction(event -> {
			projectService.inviteAdmin(project.getCommunityId(), project.getId(), inviteUser.getUserId().orElse(null));
			grid.reloadGrid();
			inviteUser.reload();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.project-admin.administrators.page.header"));
		getContent().add(headerLayout, inviteUser, grid);
	}
}
