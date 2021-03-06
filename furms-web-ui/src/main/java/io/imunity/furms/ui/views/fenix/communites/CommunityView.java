/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.fenix.communites.allocations.CommunityAllocationComponent;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.util.*;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.fenix.communites.CommunityConst.*;
import static java.util.function.Function.identity;

@Route(value = "fenix/admin/community", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.community.page.title")
public class CommunityView extends FurmsViewComponent {
	private final CommunityService communityService;
	private final UserService userService;

	private Tab defaultTab;
	private Tabs tabs;
	private Map<String, Tab> paramToTab;
	private List<RouterLink> links;
	private final PersistentId currentUserId;
	private final CommunityAllocationService allocationService;

	private BreadCrumbParameter breadCrumbParameter;

	private Div page1;
	private Div page2;

	CommunityView(CommunityService communityService, AuthzService authzService, UserService userService, CommunityAllocationService allocationService) {
		this.communityService = communityService;
		this.userService = userService;
		this.currentUserId = authzService.getCurrentUserId();
		this.allocationService = allocationService;
	}

	private void loadTabs(String communityId) {
		paramToTab = new HashMap<>();
		links = new ArrayList<>();
		page1 = new Div();
		page2 = new CommunityAllocationComponent(allocationService, communityId).getContent();

		RouterLink adminsRouterLink = new RouterLink(getTranslation("view.fenix-admin.community.tab.1"), CommunityView.class);
		adminsRouterLink.setQueryParameters(QueryParameters.simple(Map.of(PARAM_NAME, ADMINISTRATORS_PARAM)));
		Tab administratorsTab = new Tab(adminsRouterLink);
		paramToTab.put(ADMINISTRATORS_PARAM, administratorsTab);
		defaultTab = administratorsTab;
		links.add(adminsRouterLink);

		RouterLink allocRouterLink = new RouterLink(getTranslation("view.fenix-admin.community.tab.2"), CommunityView.class);
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

	private void loadPage1Content(String communityId, String communityName) {
		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> communityService.findAllAdmins(communityId)
		);
		MembershipChangerComponent membershipLayout = new MembershipChangerComponent(
			getTranslation("view.fenix-admin.community.button.join"),
			getTranslation("view.fenix-admin.community.button.demit"),
			() -> communityService.isAdmin(communityId)
		);
		UsersGridComponent grid = UsersGridComponent.builder()
			.withCurrentUserId(currentUserId)
			.withFetchUsersAction(() -> communityService.findAllAdmins(communityId))
			.withRemoveUserAction(userId -> {
				communityService.removeAdmin(communityId, userId);
				membershipLayout.loadAppropriateButton();
				inviteUser.reload();
			}).build();
		membershipLayout.addJoinButtonListener(event -> {
			communityService.addAdmin(communityId, currentUserId);
			grid.reloadGrid();
			inviteUser.reload();
		});
		membershipLayout.addDemitButtonListener(event -> {
			if (communityService.findAllAdmins(communityId).size() > 1) {
				handleExceptions(() -> communityService.removeAdmin(communityId, currentUserId));
				grid.reloadGrid();
			} else {
				showErrorNotification(getTranslation("component.administrators.error.validation.remove"));
			}
			inviteUser.reload();
			membershipLayout.loadAppropriateButton();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.fenix-admin.community.page.header", communityName),
			membershipLayout
		);
		inviteUser.addInviteAction(event -> {
			communityService.inviteAdmin(communityId, inviteUser.getUserId());
			grid.reloadGrid();
			membershipLayout.loadAppropriateButton();
			inviteUser.reload();
		});
		page1.add(headerLayout, inviteUser, grid);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String communityId) {
		getContent().removeAll();
		Community community = handleExceptions(() -> communityService.findById(communityId))
			.flatMap(identity())
			.orElseThrow(IllegalStateException::new);
		String param = event.getLocation()
			.getQueryParameters()
			.getParameters()
			.getOrDefault(PARAM_NAME, List.of(ALLOCATIONS_PARAM))
			.iterator().next();
		loadTabs(communityId);
		Tab tab = paramToTab.getOrDefault(param, defaultTab);
		tabs.setSelectedTab(tab);
		links.forEach(x -> x.setRoute(getClass(), communityId));
		breadCrumbParameter = new BreadCrumbParameter(community.getId(), community.getName(), param);
		loadPage1Content(communityId, community.getName());
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
