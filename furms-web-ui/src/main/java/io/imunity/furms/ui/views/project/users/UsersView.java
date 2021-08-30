/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.users;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsLandingViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.MembershipChangerComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.imunity.furms.domain.constant.RoutesConst.PROJECT_BASE_LANDING_PAGE;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = PROJECT_BASE_LANDING_PAGE, layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.users.page.title")
public class UsersView extends FurmsLandingViewComponent {
	
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final Predicate<FURMSUser> IS_ELIGIBLE_FOR_PROJECT_MEMBERSHIP = user -> user.fenixUserId.isPresent();
	private final ProjectService projectService;
	private final AuthzService authzService;
	private final UserService userService;
	private Project project;
	private PersistentId currentUserId;
	private MembershipChangerComponent membershipLayout;

	UsersView(ProjectService projectService, AuthzService authzService, UserService userService) {
		this.projectService = projectService;
		this.authzService = authzService;
		this.userService = userService;
		loadPageContent();
	}

	private void loadPageContent() {
		project = projectService.findById(getCurrentResourceId())
				.orElseThrow(() -> new IllegalStateException("Project not found: " + getCurrentResourceId()));
		currentUserId = authzService.getCurrentUserId();
		InviteUserComponent inviteUser = new InviteUserComponent(
			() -> userService.getAllUsers().stream()
				.filter(IS_ELIGIBLE_FOR_PROJECT_MEMBERSHIP)
				.collect(Collectors.toList()),
			() -> projectService.findAllUsers(project.getCommunityId(), project.getId())
		);
		
		membershipLayout = new MembershipChangerComponent(
				getTranslation("view.project-admin.users.button.join"),
				getTranslation("view.project-admin.users.button.demit"),
				() -> projectService.isUser(project.getId())
		);
		
		userService.findById(currentUserId).ifPresent(user -> {
			membershipLayout.setEnabled(IS_ELIGIBLE_FOR_PROJECT_MEMBERSHIP.test(user));
		});
		
		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.allowRemovalOfLastUser()
			.withConfirmRemovalMessageKey("view.project-admin.users.remove.confirm")
			.withConfirmSelfRemovalMessageKey("view.project-admin.users.remove.yourself.confirm")
			.withRemoveUserAction(userId -> {
				projectService.removeUser(project.getCommunityId(), project.getId(), userId);
				inviteUser.reload();
				membershipLayout.loadAppropriateButton();
			}).build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		UsersGridComponent grid = UsersGridComponent.defaultInit(() -> projectService.findAllUsers(project.getCommunityId(), project.getId()), userGrid);
		membershipLayout.addJoinButtonListener(event -> {
			projectService.addUser(project.getCommunityId(), project.getId(), currentUserId);
			grid.reloadGrid();
			inviteUser.reload();
		});
		membershipLayout.addDemitButtonListener(event -> {
			projectService.removeUser(project.getCommunityId(), project.getId(), currentUserId);
			grid.reloadGrid();
			inviteUser.reload();
			membershipLayout.loadAppropriateButton();
		});
		inviteUser.addInviteAction(event -> {
			projectService.inviteUser(project.getCommunityId(), project.getId(), inviteUser.getUserId().orElse(null));
			grid.reloadGrid();
			membershipLayout.loadAppropriateButton();
			inviteUser.reload();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
				getTranslation("view.project-admin.users.header", project.getName()), membershipLayout);
		getContent().add(headerLayout, inviteUser, grid);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		LOG.debug("After navigation on project users view {}", getCurrentResourceId());
		getContent().removeAll();
		loadPageContent();
	}
}
