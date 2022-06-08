/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.users;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.applications.ProjectApplicationsService;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.api.validation.exceptions.ApplicationNotExistingException;
import io.imunity.furms.api.validation.exceptions.UserAlreadyAppliedForMembershipException;
import io.imunity.furms.api.validation.exceptions.UserInstallationOnSiteIsNotTerminalException;
import io.imunity.furms.domain.authz.roles.Role;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsLandingViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.MembershipChangerComponent;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UserGridItem;
import io.imunity.furms.ui.components.administrators.UserUIStatus;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.utils.CommonExceptionsHandler;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.CHECK_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.CLOSE_CIRCLE;
import static io.imunity.furms.domain.constant.RoutesConst.PROJECT_BASE_LANDING_PAGE;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = PROJECT_BASE_LANDING_PAGE, layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.users.page.title")
public class UsersView extends FurmsLandingViewComponent {
	
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final Predicate<FURMSUser> IS_ELIGIBLE_FOR_PROJECT_MEMBERSHIP = user -> user.fenixUserId.isPresent();
	private final ProjectService projectService;
	private final ProjectApplicationsService projectApplicationsService;
	private final AuthzService authzService;
	private final UserService userService;
	private Project project;
	private PersistentId currentUserId;
	private MembershipChangerComponent membershipLayout;
	private UsersGridComponent grid;
	private UsersDAO usersDAO;

	UsersView(ProjectService projectService, AuthzService authzService, UserService userService, ProjectApplicationsService projectApplicationsService) {
		this.projectService = projectService;
		this.authzService = authzService;
		this.userService = userService;
		this.projectApplicationsService = projectApplicationsService;
	}

	private void loadPageContent() {
		ProjectId projectId = new ProjectId(getCurrentResourceId());
		project = projectService.findById(projectId)
				.orElseThrow(() -> new IllegalStateException("Project not found: " + getCurrentResourceId()));
		currentUserId = authzService.getCurrentUserId();

		usersDAO = new UsersDAO(() -> projectService.findAllProjectAdminsAndUsers(project.getCommunityId(), project.getId()));
		InviteUserComponent inviteUser = new InviteUserComponent(
			usersDAO::getProjectAdmins,
			usersDAO::getProjectUsers
		);
		
		membershipLayout = new MembershipChangerComponent(
				getTranslation("view.project-admin.users.button.join"),
				getTranslation("view.project-admin.users.button.demit"),
				() -> projectService.isUser(project.getId())
		);
		
		userService.findById(currentUserId).ifPresent(user -> membershipLayout.setEnabled(IS_ELIGIBLE_FOR_PROJECT_MEMBERSHIP.test(user)));
		
		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.allowRemovalOfLastUser()
			.withConfirmRemovalMessageKey("view.project-admin.users.remove.confirm")
			.withConfirmSelfRemovalMessageKey("view.project-admin.users.remove.yourself.confirm")
			.withRemoveInvitationAction(invitationId -> {
				projectService.removeInvitation(projectId, invitationId);
				gridReload();
			})
			.withResendInvitationAction(invitationId -> {
				projectService.resendInvitation(projectId, invitationId);
				gridReload();
			})
			.withRemoveUserAction(userId -> {
				try {
					projectService.removeUser(project.getCommunityId(), project.getId(), userId);
				} catch (UserInstallationOnSiteIsNotTerminalException e) {
					showErrorNotification(getTranslation("user.currently.de-installing"));
				}
			})
			.withPostRemoveUserAction(userId -> {
				membershipLayout.loadAppropriateButton();
				usersDAO.reload();
				inviteUser.reload();
			})
			.addCustomContextMenuItem(
				(UserGridItem userGridItem) -> new MenuButton(getTranslation("view.project-admin.users.requested.accept"), CHECK_CIRCLE),
				userGridItem -> {
				try {
					projectApplicationsService.accept(projectId, userGridItem.getFenixUserId().get());
					showSuccessNotification(getTranslation("view.project-admin.users.application.accept"));
				} catch (RuntimeException e) {
					CommonExceptionsHandler.showExceptionBasedNotificationError(e);
				}
					usersDAO.reload();
					grid.reloadGrid();
				},
				userGridItem -> UserUIStatus.ACCESS_REQUESTED.equals(userGridItem.getStatus())
			)
			.addCustomContextMenuItem(
				(UserGridItem userGridItem) -> new MenuButton(getTranslation("view.project-admin.users.requested.reject"), CLOSE_CIRCLE),
				userGridItem -> {
					try {
						projectApplicationsService.remove(projectId, userGridItem.getFenixUserId().get());
						showSuccessNotification(getTranslation("view.project-admin.users.application.reject"));
					} catch (ApplicationNotExistingException e) {
						showErrorNotification(getTranslation("application.already.not.existing"));
					}
					grid.reloadGrid();
				},
				userGridItem -> UserUIStatus.ACCESS_REQUESTED.equals(userGridItem.getStatus())
			)
			.build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(
			usersDAO::getProjectUsers,
			() -> projectService.findAllUsersInvitations(projectId),
			() -> projectApplicationsService.findAllApplyingUsers(projectId),
			userGrid
		);
		membershipLayout.addJoinButtonListener(event -> {
			projectService.addUser(project.getCommunityId(), project.getId(), currentUserId);
			usersDAO.reload();
			grid.reloadGrid();
			usersDAO.reload();
			inviteUser.reload();
		});
		membershipLayout.addDemitButtonListener(event -> {
			projectService.removeUser(project.getCommunityId(), project.getId(), currentUserId);
			usersDAO.reload();
			grid.reloadGrid();
			inviteUser.reload();
			membershipLayout.loadAppropriateButton();
		});
		inviteUser.addInviteAction(event -> doInviteAction(inviteUser, projectId));
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
				getTranslation("view.project-admin.users.header", project.getName()), membershipLayout);
		getContent().add(headerLayout, inviteUser, grid);
	}

	private void doInviteAction(InviteUserComponent inviteUserComponent, ProjectId projectId) {
		try {
			inviteUserComponent.getUserId().ifPresentOrElse(
				id -> projectService.inviteUser(projectId, id),
				() -> projectService.inviteUser(projectId, inviteUserComponent.getEmail())
			);
			usersDAO.reload();
			inviteUserComponent.reload();
			showSuccessNotification(getTranslation("invite.successful.added"));
			gridReload();
			membershipLayout.loadAppropriateButton();
		} catch (UserAlreadyAppliedForMembershipException e) {
			showErrorNotification(getTranslation("invite.error.application.exist"));
		} catch (RuntimeException e) {
			CommonExceptionsHandler.showExceptionBasedNotificationError(e);
		}
	}

	private void gridReload() {
		grid.reloadGrid();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		LOG.debug("After navigation on project users view {}", getCurrentResourceId());
		getContent().removeAll();
		loadPageContent();
	}

	private static class UsersDAO {
		private final Supplier<List<FURMSUser>> allProjectUsersGetter;
		private List<FURMSUser> projectAdmins;
		private List<FURMSUser> projectUsers;

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
				.filter(IS_ELIGIBLE_FOR_PROJECT_MEMBERSHIP)
				.filter(user -> user.roles.values().stream().anyMatch(roles -> roles.contains(Role.PROJECT_ADMIN)))
				.collect(Collectors.toList());
		}

		private List<FURMSUser> getProjectUsers(List<FURMSUser> allUsers) {
			return allUsers.stream()
				.filter(IS_ELIGIBLE_FOR_PROJECT_MEMBERSHIP)
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
