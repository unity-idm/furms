/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.administrators;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.UserStatus;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UserGridItem;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.views.landing.LandingPageView;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vaadin.flow.component.icon.VaadinIcon.EXCHANGE;

@Route(value = "site/admin/administrators", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.administrators.page.title")
public class SiteAdministratorsView extends FurmsViewComponent {
	private final SiteService siteService;

	private final UsersGridComponent grid;
	private final String siteId;

	SiteAdministratorsView(SiteService siteService, AuthzService authzService) {
		this.siteService = siteService;
		this.siteId = FurmsViewUserContext.getCurrent().id;
		PersistentId currentUserId = authzService.getCurrentUserId();
		SiteRoleInviteUserComponent inviteUser = new SiteRoleInviteUserComponent(ArrayList::new, ArrayList::new);
		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.redirectOnCurrentUserRemoval()
			.addCustomContextMenuItem(
				(SiteUserGridItem userGridItem) ->
					new MenuButton(
						getTranslation("view.site-admin.administrators.grid.role.change." + userGridItem.getSiteRole()
							.orElseThrow(() -> new IllegalArgumentException("Site role is required"))),
						EXCHANGE),
				(SiteUserGridItem userGridItem) -> {
					if (userGridItem.getStatus().equals(UserStatus.AWAITS_APPROVAL)) {
						if (userGridItem.getSiteRole().get().equals(SiteRole.SUPPORT)) {
							siteService.changeInvitationRoleToAdmin(siteId, userGridItem.getInvitationId().get());
						} else if (userGridItem.getSiteRole().get().equals(SiteRole.ADMIN)) {
							siteService.changeInvitationRoleToSupport(siteId, userGridItem.getInvitationId().get());
						}
					} else if (userGridItem.getId().isPresent() && userGridItem.getSiteRole().isPresent()){
						if(userGridItem.getSiteRole().get().equals(SiteRole.SUPPORT)) {
							siteService.changeRoleToAdmin(siteId, userGridItem.getId().get());
						} else if(userGridItem.getSiteRole().get().equals(SiteRole.ADMIN)) {
							siteService.changeRoleToSupport(siteId, userGridItem.getId().get());
							if (userGridItem.getId().get().equals(currentUserId)) {
								UI.getCurrent().navigate(LandingPageView.class);
								return;
							}
						}
					}
					inviteUser.reload();
					gridReload();
				}
			)
			.withRemoveUserAction(userId -> siteService.removeSiteUser(siteId, userId))
			.withPostRemoveUserAction(userId -> inviteUser.reload())
			.withRemoveInvitationAction(invitationId -> {
				siteService.removeInvitation(siteId, invitationId);
				gridReload();
			})
			.withResendInvitationAction(invitationId -> {
				siteService.resendInvitation(siteId, invitationId);
				gridReload();
			})
			.build();

		UserGrid.Builder userGrid = UserGrid.builder()
			.withFullNameColumn()
			.withCustomColumn((SiteUserGridItem userGridItem) ->
				getTranslation("view.site-admin.administrators.grid.role." + userGridItem.getSiteRole()
					.orElseThrow(() -> new IllegalArgumentException("Site role is required"))),
				getTranslation("view.site-admin.administrators.grid.role.column"))
			.withEmailColumn()
			.withStatusColumn()
			.withContextMenuColumn(userContextMenuFactory);
		grid = UsersGridComponent.init(this::loadUsers, this::loadInvitations, userGrid);
		doInviteAction(inviteUser);

		ViewHeaderLayout header = new ViewHeaderLayout(getTranslation("view.site-admin.administrators.title"));
		getContent().add(header, inviteUser, grid);
	}

	private void gridReload() {
		grid.reloadGrid();
	}

	private List<UserGridItem> loadUsers() {
		return Stream.of(
			siteService.findAllAdministrators(siteId).stream().map(user -> new SiteUserGridItem(user, SiteRole.ADMIN)),
			siteService.findAllSupportUsers(siteId).stream().map(user -> new SiteUserGridItem(user, SiteRole.SUPPORT))
		)
			.flatMap(Function.identity())
			.collect(Collectors.toList());
	}

	private List<UserGridItem> loadInvitations() {
		return Stream.of(
			siteService.findSiteAdminInvitations(siteId).stream()
				.map(invitation -> new SiteUserGridItem(invitation.email, SiteRole.ADMIN, invitation.id)),
			siteService.findSiteSupportInvitations(siteId).stream()
				.map(invitation -> new SiteUserGridItem(invitation.email, SiteRole.SUPPORT, invitation.id))
		)
			.flatMap(Function.identity())
			.collect(Collectors.toList());
	}

	private void doInviteAction(SiteRoleInviteUserComponent inviteUser) {
			inviteUser.addInviteAction(
				Map.of(
					SiteRole.ADMIN, (PersistentId id) -> siteService.inviteAdmin(siteId, id),
					SiteRole.SUPPORT, (PersistentId id) -> siteService.inviteSupport(siteId, id)
				),
				Map.of(
					SiteRole.ADMIN, (String email) -> siteService.inviteAdmin(siteId, email),
					SiteRole.SUPPORT, (String email) -> siteService.inviteSupport(siteId, email)
				),
				grid::reloadGrid);
	}

}
