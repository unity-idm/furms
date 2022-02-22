/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites;

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
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.validation.exceptions.DuplicatedInvitationError;
import io.imunity.furms.api.validation.exceptions.UserAlreadyHasRoleError;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.AllUsersAndCommunityAdmins;
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
import io.imunity.furms.ui.views.fenix.communites.allocations.CommunityAllocationComponent;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
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
import static io.imunity.furms.ui.views.fenix.communites.CommunityConst.ADMINISTRATORS_PARAM;
import static io.imunity.furms.ui.views.fenix.communites.CommunityConst.ALLOCATIONS_PARAM;
import static io.imunity.furms.ui.views.fenix.communites.CommunityConst.PARAM_NAME;
import static java.util.function.Function.identity;

@Route(value = "fenix/admin/community", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.community.page.title")
public class CommunityView extends FurmsViewComponent {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CommunityService communityService;

	private Tab defaultTab;
	private Tabs tabs;
	private Map<String, Tab> paramToTab;
	private List<RouterLink> links;
	private final PersistentId currentUserId;
	private final CommunityAllocationService allocationService;

	private BreadCrumbParameter breadCrumbParameter;

	private Div page1;

	private UsersGridComponent grid;
	private UsersSnapshot usersSnapshot;

	CommunityView(CommunityService communityService, AuthzService authzService, CommunityAllocationService allocationService) {
		this.communityService = communityService;
		this.currentUserId = authzService.getCurrentUserId();
		this.allocationService = allocationService;
	}

	private void loadTabs(String communityId) {
		paramToTab = new HashMap<>();
		links = new ArrayList<>();
		page1 = new Div();
		Div page2 = new CommunityAllocationComponent(allocationService, communityId).getContent();

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
			() -> usersSnapshot.allUsers,
			() -> usersSnapshot.communityAdmins
		);
		MembershipChangerComponent membershipLayout = new MembershipChangerComponent(
			getTranslation("view.fenix-admin.community.button.join"),
			getTranslation("view.fenix-admin.community.button.demit"),
			() -> communityService.isAdmin(communityId)
		);
		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.withRemoveUserAction(userId -> communityService.removeAdmin(communityId, userId))
			.withPostRemoveUserAction(userId -> {
				usersSnapshot.reload();
				membershipLayout.loadAppropriateButton();
				inviteUser.reload();
			})
			.withRemoveInvitationAction(invitationId -> {
				communityService.removeInvitation(communityId, invitationId);
				gridReload();
			})
			.withResendInvitationAction(invitationId -> {
				communityService.resendInvitation(communityId, invitationId);
				gridReload();
			})
			.build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(
			() -> usersSnapshot.communityAdmins,
			() -> communityService.findAllInvitations(communityId),
			userGrid
		);
		membershipLayout.addJoinButtonListener(event -> {
			communityService.addAdmin(communityId, currentUserId);
			gridReload();
			inviteUser.reload();
		});
		membershipLayout.addDemitButtonListener(event -> {
			if (communityService.findAllAdmins(communityId).size() > 1) {
				handleExceptions(() -> communityService.removeAdmin(communityId, currentUserId));
				gridReload();
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
		inviteUser.addInviteAction(event -> doInviteAction(communityId, inviteUser, membershipLayout));
		page1.add(headerLayout, inviteUser, grid);
	}

	private void doInviteAction(String communityId, InviteUserComponent inviteUser, MembershipChangerComponent membershipLayout) {
		try {
			inviteUser.getUserId().ifPresentOrElse(
				id -> communityService.inviteAdmin(communityId, id),
				() -> communityService.inviteAdmin(communityId, inviteUser.getEmail())
			);
			usersSnapshot.reload();
			gridReload();
			membershipLayout.loadAppropriateButton();
			inviteUser.reload();
		} catch (DuplicatedInvitationError e) {
			showErrorNotification(getTranslation("invite.error.duplicate"));
		} catch (UserAlreadyHasRoleError e) {
			showErrorNotification(getTranslation("invite.error.role.own"));
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.sites.invite.error.unexpected"));
			LOG.error("Could not invite Site Administrator. ", e);
		}
	}

	private void gridReload() {
		grid.reloadGrid();
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
		usersSnapshot = new UsersSnapshot(() -> communityService.findAllAdminsWithAllUsers(communityId));
		loadPage1Content(communityId, community.getName());
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

	static class UsersSnapshot {
		private final Supplier<AllUsersAndCommunityAdmins> allUsersGetter;
		public final List<FURMSUser> allUsers;
		public final List<FURMSUser> communityAdmins;

		UsersSnapshot(Supplier<AllUsersAndCommunityAdmins> allUsersGetter) {
			AllUsersAndCommunityAdmins allUsers = allUsersGetter.get();
			this.allUsersGetter = allUsersGetter;
			this.allUsers = allUsers.allUsers;
			this.communityAdmins = allUsers.communityAdmins;
		}

		public void reload(){
			AllUsersAndCommunityAdmins allUsers1 = allUsersGetter.get();
			allUsers.clear();
			allUsers.addAll(allUsers1.allUsers);
			communityAdmins.clear();
			communityAdmins.addAll(allUsers1.communityAdmins);
		}
	}
}
