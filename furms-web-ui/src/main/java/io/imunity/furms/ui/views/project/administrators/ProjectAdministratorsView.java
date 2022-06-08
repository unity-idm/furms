/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.administrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.utils.CommonExceptionsHandler;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "project/admin/administrators", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.administrators.page.title")
public class ProjectAdministratorsView extends FurmsViewComponent {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectService projectService;
	private final ProjectId projectId;
	private final UsersGridComponent grid;
	private final UsersDAO usersDAO;

	public ProjectAdministratorsView(ProjectService projectService, AuthzService authzService) {
		this.projectService = projectService;
		this.projectId = new ProjectId(getCurrentResourceId());

		PersistentId currentUserId = authzService.getCurrentUserId();
		Project project = projectService.findById(projectId)
				.orElseThrow(() -> new IllegalStateException("Project not found: " + projectId));

		usersDAO = new UsersDAO(() -> projectService.findAllProjectAdminsAndUsers(project.getCommunityId(), project.getId()));
		InviteUserComponent inviteUser = new InviteUserComponent(
			usersDAO::getProjectUsers,
			usersDAO::getProjectAdmins
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
			.withPostRemoveUserAction(userId -> {
				usersDAO.reload();
				inviteUser.reload();
			})
			.build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(
			usersDAO::getProjectAdmins,
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
			usersDAO.reload();
			inviteUserComponent.reload();
			showSuccessNotification(getTranslation("invite.successful.added"));
			gridReload();
		} catch (RuntimeException e) {
			CommonExceptionsHandler.showExceptionBasedNotificationError(e);
		}
	}

	private void gridReload() {
		grid.reloadGrid();
	}

	private static class UsersDAO {
		private final Supplier<List<FURMSUser>> allProjectUsersGetter;
		public List<FURMSUser> projectAdmins;
		public List<FURMSUser> projectUsers;

		UsersDAO(Supplier<List<FURMSUser>> allProjectUsersGetter) {
			this.allProjectUsersGetter = allProjectUsersGetter;
			reload();
		}

		List<FURMSUser> getProjectAdmins() {
			return projectAdmins;
		}

		List<FURMSUser> getProjectUsers() {
			return projectUsers;
		}

		private List<FURMSUser> getProjectAdmins(List<FURMSUser> allUsers) {
			return allUsers.stream()
				.filter(user -> user.roles.values().stream().anyMatch(roles -> roles.contains(Role.PROJECT_ADMIN)))
				.collect(Collectors.toList());
		}

		private List<FURMSUser> getProjectUsers(List<FURMSUser> allUsers) {
			return allUsers.stream()
				.filter(user -> user.roles.values().stream().anyMatch(roles -> roles.contains(Role.PROJECT_USER)))
				.collect(Collectors.toList());
		}

		void reload() {
			List<FURMSUser> projectUsers = allProjectUsersGetter.get();
			this.projectAdmins = getProjectAdmins(projectUsers);
			this.projectUsers = getProjectUsers(projectUsers);
		}
	}
}
