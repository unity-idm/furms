/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.adminstrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "community/admin/administrators", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.administrators.page.title")
public class CommunityAdminsView extends FurmsViewComponent {

	public CommunityAdminsView(CommunityService communityService, AuthzService authzService, UserService userService) {
		PersistentId currentUserId = authzService.getCurrentUserId();
		String communityId = getCurrentResourceId();

		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> communityService.findAllAdmins(communityId)
		);
		UsersGridComponent grid = UsersGridComponent.builder()
			.withCurrentUserId(currentUserId)
			.redirectOnCurrentUserRemoval()
			.withFetchUsersAction(() -> communityService.findAllAdmins(communityId))
			.withRemoveUserAction(userId -> {
				communityService.removeAdmin(communityId, userId);
				inviteUser.reload();
			}).build();
		inviteUser.addInviteAction(event -> {
			communityService.inviteAdmin(communityId, inviteUser.getUserId());
			grid.reloadGrid();
			inviteUser.reload();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
				getTranslation("view.community-admin.administrators.page.header"));
		getContent().add(headerLayout, inviteUser, grid);
	}

}
