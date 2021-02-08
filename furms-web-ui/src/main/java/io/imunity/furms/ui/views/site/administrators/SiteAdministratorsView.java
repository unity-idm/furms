/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.administrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.AdministratorsGridComponent;
import io.imunity.furms.ui.views.site.SiteAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;

@Route(value = "site/admin/administrators", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.administrators.page.title")
public class SiteAdministratorsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteService siteService;
	private final UserService userService;

	private final AdministratorsGridComponent grid;
	private final String siteId;

	SiteAdministratorsView(SiteService siteService, UserService userService, AuthzService authzService) {
		this.siteService = siteService;
		this.userService = userService;
		this.siteId = getActualViewUserContext().id;
		this.grid = new AdministratorsGridComponent(
				() -> siteService.findAllAdmins(siteId),
				userId -> siteService.removeAdmin(siteId, userId),
				authzService.getCurrentUserId()
			);

		addHeader();
		getContent().add(grid);
	}

	private void addHeader() {
		InviteUserComponent inviteUser = new InviteUserComponent(userService.getAllUsers());
		inviteUser.addInviteAction(event -> doInviteAction(inviteUser));

		getContent().add(new ViewHeaderLayout(getTranslation("view.site-admin.administrators.title"), inviteUser));
	}

	private void doInviteAction(InviteUserComponent inviteUser) {
		try {
			siteService.inviteAdmin(siteId, inviteUser.getEmail());
			inviteUser.clear();
			grid.reloadGrid();
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.site-admin.administrators.invite.error.unexpected"));
			LOG.error("Could not invite Site Administrator. ", e);
		}
	}

}
