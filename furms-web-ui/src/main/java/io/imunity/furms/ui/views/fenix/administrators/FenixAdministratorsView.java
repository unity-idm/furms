/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.administrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;

@Route(value = "fenix/admin/administrators", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.administrators.page.title")
public class FenixAdministratorsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UserService userService;

	private final UsersGridComponent grid;

	FenixAdministratorsView(UserService userService, AuthzService authzService) {
		this.userService = userService;

		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			userService::getFenixAdmins
		);
		inviteUser.addInviteAction(event -> doInviteAction(inviteUser));

		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(authzService.getCurrentUserId())
			.redirectOnCurrentUserRemoval()
			.withRemoveUserAction(userId -> {
				userService.removeFenixAdminRole(userId);
				inviteUser.reload();
			})
			.withResendInvitationAction(userService::resendFenixAdminInvitation)
			.withRemoveInvitationAction(code -> {
				userService.removeFenixAdminInvitation(code);
				inviteUser.reload();
			})
			.build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(userService::getFenixAdmins, userService::getFenixAdminsInvitations, userGrid);

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.fenix-admin.header"));
		
		getContent().add(headerLayout, inviteUser, grid);
	}

	private void doInviteAction(InviteUserComponent inviteUserComponent) {
		try {
			inviteUserComponent.getUserId().ifPresentOrElse(
				userService::inviteFenixAdmin,
				() -> userService.inviteFenixAdmin(inviteUserComponent.getEmail())
			);
			inviteUserComponent.reload();
			grid.reloadGrid();
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.fenix-admin.invite.error.unexpected"));
			LOG.error("Could not invite user. ", e);
		}
	}

}
