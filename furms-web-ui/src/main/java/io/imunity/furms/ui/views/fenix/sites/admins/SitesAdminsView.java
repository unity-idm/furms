/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites.admins;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.function.Function.identity;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.MembershipChangerComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

@Route(value = "fenix/admin/sites/details", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.sites.details.title")
public class SitesAdminsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteService siteService;
	private final UserService userService;
	private final String currentUserId;

	private UsersGridComponent grid;
	private String siteId;

	SitesAdminsView(SiteService siteService, UserService userService, AuthzService authzService) {
		this.siteService = siteService;
		this.userService = userService;
		this.currentUserId = authzService.getCurrentUserId();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		getContent().removeAll();
		init(parameter);
	}

	private void init(String siteId) {
		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> siteService.findAllAdmins(siteId)
		);

		MembershipChangerComponent membershipLayout = new MembershipChangerComponent(
			getTranslation("view.fenix-admin.sites.button.join"),
			getTranslation("view.fenix-admin.sites.button.demit"),
			() -> siteService.isAdmin(siteId)
		);
		membershipLayout.addJoinButtonListener(event -> {
			siteService.addAdmin(siteId, currentUserId);
			grid.reloadGrid();
			inviteUser.reload();
		});
		membershipLayout.addDemitButtonListener(event -> {
			if (siteService.findAllAdmins(siteId).size() > 1) {
				handleExceptions(() -> siteService.removeAdmin(siteId, currentUserId));
				grid.reloadGrid();
			} else {
				showErrorNotification(getTranslation("component.administrators.error.validation.remove"));
			}
			inviteUser.reload();
			membershipLayout.loadAppropriateButton();
		});

		inviteUser.addInviteAction(event -> doInviteAction(inviteUser, membershipLayout));
		this.grid = UsersGridComponent.builder()
			.withCurrentUserId(currentUserId)
			.withFetchUsersAction(() -> siteService.findAllAdmins(siteId))
			.withRemoveUserAction(userId -> {
				siteService.removeAdmin(siteId, userId);
				membershipLayout.loadAppropriateButton();
				inviteUser.reload();
			}).build();
		this.siteId = siteId;

		Site site = handleExceptions(() -> siteService.findById(siteId))
				.flatMap(identity())
				.orElseThrow(IllegalStateException::new);
		ViewHeaderLayout viewHeaderLayout = new ViewHeaderLayout(
				getTranslation("view.sites.administrators.title", site.getName()), 
				membershipLayout);
		getContent().add(viewHeaderLayout, inviteUser, grid);
	}

	private void doInviteAction(InviteUserComponent inviteUserComponent, MembershipChangerComponent membershipLayout) {
		try {
			siteService.inviteAdmin(siteId, inviteUserComponent.getUserId());
			inviteUserComponent.reload();
			membershipLayout.loadAppropriateButton();
			grid.reloadGrid();
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.sites.invite.error.unexpected"));
			LOG.error("Could not invite Site Administrator. ", e);
		}
	}

}
