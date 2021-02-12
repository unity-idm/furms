/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites.admins;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.components.administrators.AdministratorsGridComponent;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;

@Route(value = "fenix/admin/sites/details", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.sites.details.title")
public class SitesAdminsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteService siteService;
	private final UserService userService;
	private final String currentUserId;

	private AdministratorsGridComponent grid;
	private String siteId;

	SitesAdminsView(SiteService siteService, UserService userService, AuthzService authzService) {
		this.siteService = siteService;
		this.userService = userService;
		this.currentUserId = authzService.getCurrentUserId();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		init(parameter);
	}

	private void init(String siteId) {
		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> siteService.findAllAdmins(siteId)
		);
		inviteUser.addInviteAction(event -> doInviteAction(inviteUser));

		MembershipChangerComponent membershipLayout = new MembershipChangerComponent(
			getTranslation("view.fenix-admin.sites.button.join"),
			getTranslation("view.fenix-admin.sites.button.demit"),
			() -> siteService.isAdmin(siteId, currentUserId)
		);
		membershipLayout.addJoinButtonListener(event -> {
			siteService.addAdmin(siteId, currentUserId);
			grid.reloadGrid();
			UI.getCurrent().getSession().getAttribute(FurmsSelectReloader.class).reload();
		});
		membershipLayout.addDemitButtonListener(event -> {
			if (siteService.findAllAdmins(siteId).size() > 1) {
				handleExceptions(() -> siteService.removeAdmin(siteId, currentUserId));
				grid.reloadGrid();
				UI.getCurrent().getSession().getAttribute(FurmsSelectReloader.class).reload();
			} else {
				showErrorNotification(getTranslation("component.administrators.error.validation.remove"));
			}
			membershipLayout.loadAppropriateButton();
		});

		this.grid = new AdministratorsGridComponent(
				() -> siteService.findAllAdmins(siteId),
				userId -> {
					siteService.removeAdmin(siteId, userId);
					inviteUser.reload();
				},
				currentUserId
		);
		this.siteId = siteId;

		ViewHeaderLayout viewHeaderLayout = new ViewHeaderLayout(getTranslation("view.sites.administrators.title"), membershipLayout);
		getContent().add(viewHeaderLayout, inviteUser, grid);
	}

	private void doInviteAction(InviteUserComponent inviteUserComponent) {
		try {
			siteService.inviteAdmin(siteId, inviteUserComponent.getEmail());
			inviteUserComponent.reload();
			grid.reloadGrid();
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.sites.invite.error.unexpected"));
			LOG.error("Could not invite Site Administrator. ", e);
		}
	}

}
