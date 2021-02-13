/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.administrators;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.router.Route;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.AdministratorsGridComponent;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

@Route(value = "fenix/admin/administrators", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.administrators.page.title")
public class FenixAdministratorsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UserService userService;

	private final AdministratorsGridComponent grid;

	FenixAdministratorsView(UserService userService, AuthzService authzService) {
		this.userService = userService;

		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			userService::getFenixAdmins
		);
		inviteUser.addInviteAction(event -> doInviteAction(inviteUser));

		this.grid = new AdministratorsGridComponent(
			userService::getFenixAdmins,
			userId -> {
				userService.removeFenixAdminRole(userId);
				inviteUser.reload();
			},
			authzService.getCurrentUserId(),
			true
		);
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.fenix-admin.header"));
		
		getContent().add(headerLayout, inviteUser, grid);
	}

	private void doInviteAction(InviteUserComponent inviteUserComponent) {
		try {
			userService.inviteFenixAdmin(inviteUserComponent.getEmail());
			inviteUserComponent.reload();
			grid.reloadGrid();
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.fenix-admin.invite.error.unexpected"));
			LOG.error("Could not invite user. ", e);
		}
	}

}
