/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.administrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.SiteUserGridItem;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UserGridItem;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.views.site.SiteAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.vaadin.flow.component.icon.VaadinIcon.EXCHANGE;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;

@Route(value = "site/admin/administrators", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.administrators.page.title")
public class SiteAdministratorsView extends FurmsViewComponent {
	
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteService siteService;

	private final UsersGridComponent grid;
	private final String siteId;

	SiteAdministratorsView(SiteService siteService, UserService userService, AuthzService authzService) {
		this.siteService = siteService;
		this.siteId = FurmsViewUserContext.getCurrent().id;
		PersistentId currentUserId = authzService.getCurrentUserId();
		SiteRoleInviteUserComponent inviteUser = new SiteRoleInviteUserComponent(
			userService::getAllUsers,
			() -> siteService.findAllSiteUsers(siteId)
		);
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
					if(userGridItem.getId().isPresent() && userGridItem.getSiteRole().isPresent()){
						siteService.removeSiteUser(siteId, userGridItem.getId().get());
						if(userGridItem.getSiteRole().get().equals(SiteRole.SUPPORT))
							siteService.addAdmin(siteId, userGridItem.getId().get());
						else if(userGridItem.getSiteRole().get().equals(SiteRole.ADMIN))
							siteService.addSupport(siteId, userGridItem.getId().get());
					}
				}
			)
			.withRemoveUserAction(userId -> {
				siteService.removeSiteUser(siteId, userId);
				inviteUser.reload();
			}).build();

		UserGrid.Builder userGrid = UserGrid.builder()
			.withFullNameColumn()
			.withCustomColumn((SiteUserGridItem userGridItem) ->
				getTranslation("view.site-admin.administrators.grid.role." + userGridItem.getSiteRole()
					.orElseThrow(() -> new IllegalArgumentException("Site role is required"))),
				getTranslation("view.site-admin.administrators.grid.role.column"))
			.withEmailColumn()
			.withStatusColumn()
			.withContextMenuColumn(userContextMenuFactory);
		grid = UsersGridComponent.init(this::loadUsers, userGrid);
		doInviteAction(inviteUser);

		ViewHeaderLayout header = new ViewHeaderLayout(getTranslation("view.site-admin.administrators.title"));
		getContent().add(header, inviteUser, grid);
	}

	private List<UserGridItem> loadUsers() {
		return Stream.of(
			siteService.findAllAdministrators(siteId).stream().map(user -> new SiteUserGridItem(user, SiteRole.ADMIN)),
			siteService.findAllSupportUsers(siteId).stream().map(user -> new SiteUserGridItem(user, SiteRole.SUPPORT))
		)
			.flatMap(Function.identity())
			.collect(Collectors.toList());
	}
	private void doInviteAction(SiteRoleInviteUserComponent inviteUser) {
		try {
			inviteUser.addInviteAction(Map.of(
				SiteRole.ADMIN, (PersistentId id) -> siteService.inviteAdmin(siteId, id),
				SiteRole.SUPPORT, (PersistentId id) -> siteService.inviteSupport(siteId, id)
			), grid::reloadGrid);
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.site-admin.administrators.invite.error.unexpected"));
			LOG.warn("Could not invite Site Administrator. ", e);
		}
	}

}
