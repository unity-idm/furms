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
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.components.administrators.AdministratorsGridComponent;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import static io.imunity.furms.domain.constant.RoutesConst.PROJECT_BASE_LANDING_PAGE;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = PROJECT_BASE_LANDING_PAGE, layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.users.page.title")
public class UsersView extends FurmsLandingViewComponent {
	private final ProjectService projectService;
	private final AuthzService authzService;
	private final UserService userService;
	private Project project;
	private String currentUserId;
	private MembershipChangerComponent membershipLayout;

	UsersView(ProjectService projectService, AuthzService authzService, UserService userService) {
		this.projectService = projectService;
		this.authzService = authzService;
		this.userService = userService;
		loadPageContent();
	}

	private void loadPageContent() {
		project = projectService.findById(getCurrentResourceId()).get();
		AdministratorsGridComponent grid = new AdministratorsGridComponent(
			() -> projectService.findAllUsers(project.getCommunityId(), project.getId()),
			userId -> projectService.removeUser(project.getCommunityId(), project.getId(), userId),
			currentUserId
		);

		currentUserId = authzService.getCurrentUserId();
		membershipLayout = new MembershipChangerComponent(
			getTranslation("view.project-admin.users.button.join"),
			getTranslation("view.project-admin.users.button.demit"),
			() -> projectService.isUser(project.getCommunityId(), project.getId(), currentUserId)
		);
		membershipLayout.addJoinButtonListener(event -> {
			projectService.addUser(project.getCommunityId(), project.getId(), currentUserId);
			grid.reloadGrid();
		});
		membershipLayout.addDemitButtonListener(event -> {
			projectService.removeUser(project.getCommunityId(), project.getId(), currentUserId);
			grid.reloadGrid();
		});
		InviteUserComponent inviteUser = new InviteUserComponent(userService.getAllUsers());
		inviteUser.addInviteAction(event -> {
			projectService.inviteUser(project.getCommunityId(), project.getId(), inviteUser.getEmail());
			grid.reloadGrid();
			membershipLayout.loadAppropriateButton();
			inviteUser.clear();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.project-admin.users.header", project.getName()), membershipLayout);
		getContent().add(headerLayout, inviteUser, grid);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		getContent().removeAll();
		loadPageContent();
	}
}
