/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.adminstrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.validation.exceptions.DuplicatedInvitationError;
import io.imunity.furms.api.validation.exceptions.UserAlreadyHasRoleError;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.GroupedUsers;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Supplier;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "community/admin/administrators", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.administrators.page.title")
public class CommunityAdminsView extends FurmsViewComponent {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CommunityService communityService;
	private final UsersGridComponent grid;
	private final String communityId;
	private final UsersSnapshot usersSnapshot;

	public CommunityAdminsView(CommunityService communityService, AuthzService authzService) {
		this.communityService = communityService;
		communityId = getCurrentResourceId();
		PersistentId currentUserId = authzService.getCurrentUserId();

		usersSnapshot = new UsersSnapshot(() -> communityService.findAllCommunityAdminsAllUsers(communityId));
		InviteUserComponent inviteUser = new InviteUserComponent(
			() -> usersSnapshot.communityUsers,
			() -> usersSnapshot.communityAdmins
		);

		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.redirectOnCurrentUserRemoval()
			.withRemoveUserAction(userId -> communityService.removeAdmin(communityId, userId))
			.withPostRemoveUserAction(userId -> {
				usersSnapshot.reload();
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
		grid = UsersGridComponent.defaultInit(() -> usersSnapshot.communityAdmins, () -> communityService.findAllInvitations(communityId), userGrid);

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
			usersSnapshot.reload();
			inviteUserComponent.reload();
			showSuccessNotification(getTranslation("invite.successful.added"));
			gridReload();
		} catch (DuplicatedInvitationError e) {
			showErrorNotification(getTranslation("invite.error.duplicate"));
		} catch (UserAlreadyHasRoleError e) {
			showErrorNotification(getTranslation("invite.error.role.own"));
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("invite.error.unexpected"));
			LOG.error("Could not invite user. ", e);
		}
	}

	private void gridReload() {
		grid.reloadGrid();
	}

	static class UsersSnapshot {
		private final Supplier<GroupedUsers> allUsersGetter;
		public final List<FURMSUser> communityAdmins;
		public final List<FURMSUser> communityUsers;

		UsersSnapshot(Supplier<GroupedUsers> allUsersGetter) {
			GroupedUsers allUsers = allUsersGetter.get();
			this.allUsersGetter = allUsersGetter;
			this.communityAdmins = allUsers.firstUsersGroup;
			this.communityUsers = allUsers.secondUsersGroup;
		}

		public void reload(){
			GroupedUsers allUsers1 = allUsersGetter.get();
			communityAdmins.clear();
			communityAdmins.addAll(allUsers1.firstUsersGroup);
			communityUsers.clear();
			communityUsers.addAll(allUsers1.secondUsersGroup);
		}
	}
}
