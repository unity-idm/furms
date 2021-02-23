/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.util.*;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.*;
import static java.util.function.Function.identity;

@Route(value = "community/admin/project", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.project.page.title")
public class ProjectView extends FurmsViewComponent {
	private final ProjectService projectService;
	private final UserService userService;
	private final String currentUserId;

	private Tab defaultTab;
	private Tabs tabs;
	private Map<String, Tab> paramToTab;
	private List<RouterLink> links;

	private BreadCrumbParameter breadCrumbParameter;

	private Div page1;
	private Div page2;

	ProjectView(ProjectService projectService, AuthzService authzService, UserService userService) {
		this.projectService = projectService;
		this.userService = userService;
		this.currentUserId = authzService.getCurrentUserId();
	}

	private void loadTabs() {
		paramToTab = new HashMap<>();
		links = new ArrayList<>();
		page1 = new Div();
		page2 = new Div();
		RouterLink adminsRouterLink = new RouterLink(getTranslation("view.community-admin.project.tab.1"), ProjectView.class);
		adminsRouterLink.setQueryParameters(QueryParameters.simple(Map.of(PARAM_NAME, ADMINISTRATORS_PARAM)));
		Tab administratorsTab = new Tab(adminsRouterLink);
		paramToTab.put(ADMINISTRATORS_PARAM, administratorsTab);
		defaultTab = administratorsTab;
		links.add(adminsRouterLink);

		RouterLink allocRouterLink = new RouterLink(getTranslation("view.community-admin.project.tab.2"), ProjectView.class);
		allocRouterLink.setQueryParameters(QueryParameters.simple(Map.of(PARAM_NAME, ALLOCATIONS_PARAM)));
		Tab allocationsTab = new Tab(allocRouterLink);
		paramToTab.put(ALLOCATIONS_PARAM, allocationsTab);
		links.add(allocRouterLink);

		page2.setText("Page#2");
		page2.setVisible(false);

		Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(administratorsTab, page1);
		tabsToPages.put(allocationsTab, page2);

		tabs = new FurmsTabs(administratorsTab, allocationsTab);
		Div pages = new Div(page1, page2);

		tabs.addSelectedChangeListener(event -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
			selectedPage.setVisible(true);

		});

		getContent().add(tabs, pages);
	}

	private void loadPage1Content(Project project) {
		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> projectService.findAllAdmins(project.getCommunityId(), project.getId())
		);
		MembershipChangerComponent membershipLayout = new MembershipChangerComponent(
			getTranslation("view.community-admin.project.button.join"),
			getTranslation("view.community-admin.project.button.demit"),
			() -> projectService.isAdmin(project.getCommunityId(), project.getId(), currentUserId)
		);
		UsersGridComponent grid = UsersGridComponent.builder()
			.withCurrentUserId(currentUserId)
			.withFetchUsersAction(() -> projectService.findAllAdmins(project.getCommunityId(), project.getId()))
			.withRemoveUserAction(userId -> {
				projectService.removeAdmin(project.getCommunityId(), project.getId(), userId);
				membershipLayout.loadAppropriateButton();
				inviteUser.reload();
			}).build();
		membershipLayout.addJoinButtonListener(event -> {
			projectService.addAdmin(project.getCommunityId(), project.getId(), currentUserId);
			grid.reloadGrid();
			inviteUser.reload();
		});
		membershipLayout.addDemitButtonListener(event -> {
			if (projectService.findAllAdmins(project.getCommunityId(), project.getId()).size() > 1) {
				handleExceptions(() -> projectService.removeAdmin(project.getCommunityId(), project.getId(), currentUserId));
				grid.reloadGrid();
			} else {
				showErrorNotification(getTranslation("component.administrators.error.validation.remove"));
			}
			inviteUser.reload();
			membershipLayout.loadAppropriateButton();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.community-admin.project.page.header", project.getName()),
			membershipLayout
		);
		inviteUser.addInviteAction(event -> {
			projectService.inviteAdmin(project.getCommunityId(), project.getId(), inviteUser.getUserId());
			grid.reloadGrid();
			membershipLayout.loadAppropriateButton();
			inviteUser.reload();
		});
		page1.add(headerLayout, inviteUser, grid);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String projectId) {
		getContent().removeAll();
		Project project = handleExceptions(() -> projectService.findById(projectId))
			.flatMap(identity())
			.orElseThrow(IllegalStateException::new);
		String param = event.getLocation()
			.getQueryParameters()
			.getParameters()
			.getOrDefault(PARAM_NAME, List.of(ADMINISTRATORS_PARAM))
			.iterator().next();
		loadTabs();
		Tab tab = paramToTab.getOrDefault(param, defaultTab);
		tabs.setSelectedTab(tab);
		links.forEach(x -> x.setRoute(getClass(), projectId));
		breadCrumbParameter = new BreadCrumbParameter(project.getId(), project.getName(), param);
		loadPage1Content(project);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
