/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.administrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.users.FenixUserService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.api.validation.exceptions.DuplicatedInvitationError;
import io.imunity.furms.api.validation.exceptions.InvalidEmailException;
import io.imunity.furms.api.validation.exceptions.UserAlreadyHasRoleError;
import io.imunity.furms.domain.users.AllUsersAndFenixAdmins;
import io.imunity.furms.domain.users.FURMSUser;
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
import java.util.List;
import java.util.function.Supplier;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;

@Route(value = "fenix/admin/administrators", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.administrators.page.title")
public class FenixAdministratorsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final FenixUserService fenixUserService;
	private final UsersDAO usersDAO;

	private final UsersGridComponent grid;

	FenixAdministratorsView(UserService userService, FenixUserService fenixUserService, AuthzService authzService) {
		this.fenixUserService = fenixUserService;
		this.usersDAO = new UsersDAO(userService::getAllUsersAndFenixAdmins);

		InviteUserComponent inviteUser = new InviteUserComponent(
			usersDAO::getAllUsers,
			usersDAO::getFenixAdmins
		);
		inviteUser.addInviteAction(event -> doInviteAction(inviteUser));

		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(authzService.getCurrentUserId())
			.redirectOnCurrentUserRemoval()
			.withRemoveUserAction(fenixUserService::removeFenixAdminRole)
			.withPostRemoveUserAction(userId -> {
				usersDAO.reload();
				inviteUser.reload();
			})
			.withResendInvitationAction(fenixUserService::resendFenixAdminInvitation)
			.withRemoveInvitationAction(code -> {
				fenixUserService.removeFenixAdminInvitation(code);
				gridReload();
			})
			.build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(usersDAO::getFenixAdmins, fenixUserService::getFenixAdminsInvitations, userGrid);

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.fenix-admin.header"));
		
		getContent().add(headerLayout, inviteUser, grid);
	}

	private void gridReload() {
		grid.reloadGrid();
	}

	private void doInviteAction(InviteUserComponent inviteUserComponent) {
		try {
			inviteUserComponent.getUserId().ifPresentOrElse(
				fenixUserService::inviteFenixAdmin,
				() -> fenixUserService.inviteFenixAdmin(inviteUserComponent.getEmail())
			);
			usersDAO.reload();
			inviteUserComponent.reload();
			showSuccessNotification(getTranslation("invite.successful.added"));
			gridReload();
		} catch (DuplicatedInvitationError e) {
				showErrorNotification(getTranslation("invite.error.duplicate"));
		} catch (UserAlreadyHasRoleError e) {
			showErrorNotification(getTranslation("invite.error.role.own"));
		} catch (InvalidEmailException e) {
			showErrorNotification(getTranslation("invite.error.email"));
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("invite.error.unexpected"));
			LOG.error("Could not invite user. ", e);
		}
	}

	private static class UsersDAO {
		private final Supplier<AllUsersAndFenixAdmins> allUsersGetter;
		private AllUsersAndFenixAdmins allUsersAndFenixAdmins;

		UsersDAO(Supplier<AllUsersAndFenixAdmins> allUsersGetter) {
			this.allUsersGetter = allUsersGetter;
			this.allUsersAndFenixAdmins = allUsersGetter.get();
		}

		void reload(){
			allUsersAndFenixAdmins = allUsersGetter.get();
		}

		List<FURMSUser> getAllUsers() {
			return allUsersAndFenixAdmins.allUsers;
		}

		List<FURMSUser> getFenixAdmins() {
			return allUsersAndFenixAdmins.fenixAdmins;
		}
	}
}
