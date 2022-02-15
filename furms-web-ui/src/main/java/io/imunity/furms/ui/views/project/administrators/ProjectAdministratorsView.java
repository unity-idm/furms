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
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
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
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
	private final UsersSnapshot usersSnapshot;

	public ProjectAdministratorsView(ProjectService projectService, AuthzService authzService) {
		this.projectService = projectService;
		this.projectId = getCurrentResourceId();

		PersistentId currentUserId = authzService.getCurrentUserId();
		Project project = projectService.findById(projectId)
				.orElseThrow(() -> new IllegalStateException("Project not found: " + projectId));

		usersSnapshot = new UsersSnapshot(() -> projectService.findAllProjectAdminsAndUsers(project.getCommunityId(), project.getId()));
		InviteUserComponent inviteUser = new InviteUserComponent(
			() -> usersSnapshot.projectUsers,
			() -> usersSnapshot.projectAdmins
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
				usersSnapshot.reload();
				inviteUser.reload();
			})
			.build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(
			() -> usersSnapshot.projectAdmins,
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
			usersSnapshot.reload();
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

	static class UsersSnapshot {
		private final Supplier<List<FURMSUser>> allUsersGetter;
		public final List<FURMSUser> projectAdmins;
		public final List<FURMSUser> projectUsers;

		UsersSnapshot(Supplier<List<FURMSUser>> allUsersGetter) {
			this.allUsersGetter = allUsersGetter;
			List<FURMSUser> allUsers = allUsersGetter.get();
			Map<Boolean, List<FURMSUser>> collect = partitioningByRole(allUsers);
			this.projectAdmins = collect.get(true);
			this.projectUsers = collect.get(false);
		}

		private Map<Boolean, List<FURMSUser>> partitioningByRole(List<FURMSUser> allUsers) {
			return allUsers.stream()
				.collect(Collectors.partitioningBy(x -> x.roles.values().stream().anyMatch(y -> y.contains(Role.PROJECT_ADMIN))));
		}

		public void reload(){
			List<FURMSUser> allUsers = allUsersGetter.get();
			Map<Boolean, List<FURMSUser>> collect = partitioningByRole(allUsers);
			projectAdmins.clear();
			projectAdmins.addAll(collect.get(true));
			projectUsers.clear();
			projectUsers.addAll(collect.get(false));
		}
	}
}
