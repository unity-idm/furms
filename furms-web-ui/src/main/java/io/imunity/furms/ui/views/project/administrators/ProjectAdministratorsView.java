/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.administrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.validation.exceptions.DuplicatedInvitationError;
import io.imunity.furms.api.validation.exceptions.UserAlreadyHasRoleError;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "project/admin/administrators", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.administrators.page.title")
public class ProjectAdministratorsView extends FurmsViewComponent {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectService projectService;
	private final String projectId;
	private final UsersGridComponent grid;

	public ProjectAdministratorsView(ProjectService projectService, AuthzService authzService) {
		this.projectService = projectService;
		this.projectId = getCurrentResourceId();

		PersistentId currentUserId = authzService.getCurrentUserId();
		Project project = projectService.findById(projectId)
				.orElseThrow(() -> new IllegalStateException("Project not found: " + projectId));

		InviteUserComponent inviteUser = new InviteUserComponent(
			() -> projectService.findAllUsers(project.getCommunityId(), project.getId()),
			() -> projectService.findAllAdmins(project.getCommunityId(), project.getId())
		);
		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.redirectOnCurrentUserRemoval()
			.withRemoveInvitationAction(invitationId -> {
				projectService.removeInvitation(projectId, invitationId);
				gridReload();
			})
			.withResendInvitationAction(invitationId -> {
				projectService.resendInvitation(projectId, invitationId);
				gridReload();
			})
			.withRemoveUserAction(userId -> projectService.removeAdmin(project.getCommunityId(), project.getId(), userId))
			.withPostRemoveUserAction(userId -> inviteUser.reload())
			.build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(() -> projectService.findAllAdmins(project.getCommunityId(), project.getId()),
			() -> projectService.findAllAdminsInvitations(projectId),
			userGrid);

		inviteUser.addInviteAction(event -> doInviteAction(inviteUser));
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.project-admin.administrators.page.header"));
		getContent().add(headerLayout, inviteUser, grid);
	}

	private void doInviteAction(InviteUserComponent inviteUserComponent) {
		try {
			inviteUserComponent.getUserId().ifPresentOrElse(
				id -> projectService.inviteAdmin(projectId, id),
				() -> projectService.inviteAdmin(projectId, inviteUserComponent.getEmail())
			);
			inviteUserComponent.reload();
			showSuccessNotification(getTranslation("invite.successful.added"));
			gridReload();
		} catch (DuplicatedInvitationError e) {
			showErrorNotification(getTranslation("invite.error.duplicate"));
		} catch (UserAlreadyHasRoleError e) {
			showErrorNotification(getTranslation("invite.error.role.own"));
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("invite.error.unexpected"));
			LOG.error("Could not invite user. ", e);
		}
	}

	private void gridReload() {
		grid.reloadGrid();
	}
}
