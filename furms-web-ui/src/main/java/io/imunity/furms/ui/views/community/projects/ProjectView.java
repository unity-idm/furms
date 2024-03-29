/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.CommunityAdminsAndProjectAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsTabs;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.MembershipChangerComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.utils.CommonExceptionsHandler;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;
import io.imunity.furms.ui.views.community.projects.allocations.ProjectAllocationComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.ADMINISTRATORS_PARAM;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.ALLOCATIONS_PARAM;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.PARAM_NAME;
import static java.util.function.Function.identity;

@Route(value = "community/admin/project", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.project.page.title")
public class ProjectView extends FurmsViewComponent {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ProjectService projectService;
	private final PersistentId currentUserId;
	private final ProjectAllocationService projectAllocationService;

	private Tab defaultTab;
	private Tabs tabs;
	private Map<String, Tab> paramToTab;
	private List<RouterLink> links;

	private BreadCrumbParameter breadCrumbParameter;

	private Div page1;

	private UsersGridComponent grid;
	private UsersDAO usersDAO;

	ProjectView(ProjectService projectService, AuthzService authzService, ProjectAllocationService projectAllocationService) {
		this.projectService = projectService;
		this.currentUserId = authzService.getCurrentUserId();
		this.projectAllocationService = projectAllocationService;
	}

	private void loadTabs(ProjectId projectId) {
		paramToTab = new HashMap<>();
		links = new ArrayList<>();
		page1 = new Div();
		Div page2 = new ProjectAllocationComponent(projectService, projectAllocationService, projectId).getContent();
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
		usersDAO = new UsersDAO(() -> projectService.findAllCommunityAndProjectAdmins(project.getCommunityId(), project.getId()));
		InviteUserComponent inviteUser = new InviteUserComponent(
			usersDAO::getCommunityAdmins,
			usersDAO::getProjectUsers
		);
		MembershipChangerComponent membershipLayout = new MembershipChangerComponent(
			getTranslation("view.community-admin.project.button.join"),
			getTranslation("view.community-admin.project.button.demit"),
			() -> projectService.isAdmin(project.getId())
		);
		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.redirectOnCurrentUserRemoval()
			.withRemoveUserAction(userId -> projectService.removeAdmin(project.getCommunityId(), project.getId(), userId))
			.withRemoveInvitationAction(invitationId -> {
				projectService.removeInvitation(project.getId(), invitationId);
				gridReload();
			})
			.withResendInvitationAction(invitationId -> {
				projectService.resendInvitation(project.getId(), invitationId);
				gridReload();
			})
			.withPostRemoveUserAction(userId -> {
				membershipLayout.loadAppropriateButton();
				usersDAO.reload();
				inviteUser.reload();
			})
			.build();

		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(
			usersDAO::getProjectUsers,
			() -> projectService.findAllAdminsInvitations(project.getId()),
			userGrid
		);

		membershipLayout.addJoinButtonListener(event -> {
			projectService.addAdmin(project.getCommunityId(), project.getId(), currentUserId);
			usersDAO.reload();
			gridReload();
			inviteUser.reload();
		});
		membershipLayout.addDemitButtonListener(event -> {
			if (projectService.findAllAdmins(project.getCommunityId(), project.getId()).size() > 1) {
				handleExceptions(() -> projectService.removeAdmin(project.getCommunityId(), project.getId(), currentUserId));
			} else {
				showErrorNotification(getTranslation("component.administrators.error.validation.remove"));
			}
			usersDAO.reload();
			inviteUser.reload();
			membershipLayout.loadAppropriateButton();
			gridReload();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.community-admin.project.page.header", project.getName()),
			membershipLayout
		);
		inviteUser.addInviteAction(event -> doInviteAction(project, inviteUser, membershipLayout));
		page1.add(headerLayout, inviteUser, grid);
	}

	private void doInviteAction(Project project, InviteUserComponent inviteUser, MembershipChangerComponent membershipLayout) {
		try {
			inviteUser.getUserId().ifPresentOrElse(
				id -> projectService.inviteAdmin(project.getId(), id),
				() -> projectService.inviteAdmin(project.getId(), inviteUser.getEmail())
			);
			usersDAO.reload();
			gridReload();
			membershipLayout.loadAppropriateButton();
			inviteUser.reload();
		} catch (RuntimeException e) {
			boolean handled = CommonExceptionsHandler.showExceptionBasedNotificationError(e);
			if(!handled)
				LOG.error("Could not invite project user.");
		}
	}

	private void gridReload() {
		grid.reloadGrid();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String projectId) {
		getContent().removeAll();
		ProjectId pId = new ProjectId(projectId);
		Project project = handleExceptions(() -> projectService.findById(pId))
			.flatMap(identity())
			.orElseThrow(IllegalStateException::new);
		String param = event.getLocation()
			.getQueryParameters()
			.getParameters()
			.getOrDefault(PARAM_NAME, List.of(ALLOCATIONS_PARAM))
			.iterator().next();
		loadTabs(pId);
		Tab tab = paramToTab.getOrDefault(param, defaultTab);
		tabs.setSelectedTab(tab);
		links.forEach(x -> x.setRoute(getClass(), projectId));
		breadCrumbParameter = new BreadCrumbParameter(project.getId().id.toString(), project.getName(), param);
		loadPage1Content(project);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

	private static class UsersDAO {
		private final Supplier<CommunityAdminsAndProjectAdmins> communityAdminsAndProjectAdminsSupplier;
		private CommunityAdminsAndProjectAdmins currentSnapshot;

		UsersDAO(Supplier<CommunityAdminsAndProjectAdmins> communityAdminsAndProjectAdminsSupplier) {
			this.communityAdminsAndProjectAdminsSupplier = communityAdminsAndProjectAdminsSupplier;
			reload();
		}

		void reload() {
			currentSnapshot = communityAdminsAndProjectAdminsSupplier.get();
		}

		List<FURMSUser> getCommunityAdmins() {
			return currentSnapshot.communityAdmins;
		}

		List<FURMSUser> getProjectUsers() {
			return currentSnapshot.projectAdmins;
		}
	}
}
