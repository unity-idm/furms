/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.adminstrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.components.administrators.AdministratorsGridComponent;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "community/admin/administrators", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.administrators.page.title")
public class CommunityAdminsView extends FurmsViewComponent {

	public CommunityAdminsView(CommunityService communityService, AuthzService authzService, UserService userService) {
		String currentUserId = authzService.getCurrentUserId();
		String communityId = getCurrentResourceId();

		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> communityService.findAllAdmins(communityId)
		);
		AdministratorsGridComponent grid = new AdministratorsGridComponent(
			() -> communityService.findAllAdmins(communityId),
			userId -> {
				communityService.removeAdmin(communityId, userId);
				inviteUser.reload();
			},
			currentUserId,
			true
		);
		inviteUser.addInviteAction(event -> {
			communityService.inviteAdmin(communityId, inviteUser.getEmail());
			grid.reloadGrid();
			inviteUser.reload();
		});
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.community-admin.administrators.page.header"),
			inviteUser
		);
		getContent().add(headerLayout, grid);
	}

}