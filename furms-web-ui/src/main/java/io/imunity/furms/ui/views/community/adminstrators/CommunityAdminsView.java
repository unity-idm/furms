/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.adminstrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.users.CommunityUsersAndAdmins;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.utils.CommonExceptionsHandler;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Supplier;

import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "community/admin/administrators", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.administrators.page.title")
public class CommunityAdminsView extends FurmsViewComponent {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CommunityService communityService;
	private final UsersGridComponent grid;
	private final String communityId;
	private final UsersDAO usersDAO;

	public CommunityAdminsView(CommunityService communityService, AuthzService authzService) {
		this.communityService = communityService;
		communityId = getCurrentResourceId();
		PersistentId currentUserId = authzService.getCurrentUserId();

		usersDAO = new UsersDAO(() -> communityService.findAllCommunityAdminsAllUsers(communityId));
		InviteUserComponent inviteUser = new InviteUserComponent(
			usersDAO::getCommunityUsers,
			usersDAO::getCommunityAdmins
		);

		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.redirectOnCurrentUserRemoval()
			.withRemoveUserAction(userId -> communityService.removeAdmin(communityId, userId))
			.withPostRemoveUserAction(userId -> {
				usersDAO.reload();
				inviteUser.reload();
			})
			.withResendInvitationAction(invitationId -> {
				communityService.resendInvitation(communityId, invitationId);
				gridReload();
			})
			.withRemoveInvitationAction(invitationId -> {
				communityService.removeInvitation(communityId, invitationId);
				gridReload();
			})
			.build();
		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(usersDAO::getCommunityAdmins,
			() -> communityService.findAllInvitations(communityId), userGrid);

		inviteUser.addInviteAction(event -> doInviteAction(inviteUser));
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
				getTranslation("view.community-admin.administrators.page.header"));
		getContent().add(headerLayout, inviteUser, grid);
	}

	private void doInviteAction(InviteUserComponent inviteUserComponent) {
		try {
			inviteUserComponent.getUserId().ifPresentOrElse(
				id -> communityService.inviteAdmin(communityId, id),
				() -> communityService.inviteAdmin(communityId, inviteUserComponent.getEmail())
			);
			usersDAO.reload();
			inviteUserComponent.reload();
			showSuccessNotification(getTranslation("invite.successful.added"));
			gridReload();
		} catch (RuntimeException e) {
			CommonExceptionsHandler.showExceptionBasedNotificationError(e);
		}
	}

	private void gridReload() {
		grid.reloadGrid();
	}

	private static class UsersDAO {
		private final Supplier<CommunityUsersAndAdmins> communityUsersAndAdminsSupplier;
		private CommunityUsersAndAdmins currentSnapshot;

		UsersDAO(Supplier<CommunityUsersAndAdmins> communityUsersAndAdminsSupplier) {
			this.communityUsersAndAdminsSupplier = communityUsersAndAdminsSupplier;
			reload();
		}

		void reload(){
			currentSnapshot = communityUsersAndAdminsSupplier.get();
		}

		List<FURMSUser> getCommunityAdmins() {
			return currentSnapshot.communityAdmins;
		}

		List<FURMSUser> getCommunityUsers() {
			return currentSnapshot.communityUsers;
		}
	}
}
