/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.administrators;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.router.Route;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

@Route(value = "site/admin/administrators", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.administrators.page.title")
public class SiteAdministratorsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteService siteService;

	private final UsersGridComponent grid;
	private final String siteId;
	private final PersistentId currentUserId;

	SiteAdministratorsView(SiteService siteService, UserService userService, AuthzService authzService) {
		this.siteService = siteService;
		this.siteId = getActualViewUserContext().id;
		this.currentUserId = authzService.getCurrentUserId();
		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> siteService.findAllAdmins(siteId)
		);
		inviteUser.addInviteAction(event -> doInviteAction(inviteUser));
		this.grid = UsersGridComponent.builder()
			.withCurrentUserId(currentUserId)
			.redirectOnCurrentUserRemoval()
			.withFetchUsersAction(() -> siteService.findAllAdmins(siteId))
			.withRemoveUserAction(userId -> {
				siteService.removeAdmin(siteId, userId);
				inviteUser.reload();
			}).build();

		ViewHeaderLayout header = new ViewHeaderLayout(getTranslation("view.site-admin.administrators.title"));
		getContent().add(header, inviteUser, grid);
	}

	private void doInviteAction(InviteUserComponent inviteUser) {
		try {
			siteService.inviteAdmin(siteId, inviteUser.getUserId());
			inviteUser.reload();
			grid.reloadGrid();
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.site-admin.administrators.invite.error.unexpected"));
			LOG.warn("Could not invite Site Administrator. ", e);
		}
	}

}
