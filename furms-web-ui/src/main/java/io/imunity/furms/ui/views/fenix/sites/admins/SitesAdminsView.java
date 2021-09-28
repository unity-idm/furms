/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites.admins;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.api.validation.exceptions.DuplicatedInvitationError;
import io.imunity.furms.api.validation.exceptions.UserAlreadyHasRoleError;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.MembershipChangerComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.UserContextMenuFactory;
import io.imunity.furms.ui.components.administrators.UserGrid;
import io.imunity.furms.ui.components.administrators.UsersGridComponent;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.function.Function.identity;

@Route(value = "fenix/admin/sites/details", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.sites.details.title")
public class SitesAdminsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteService siteService;
	private final UserService userService;
	private final PersistentId currentUserId;
	private String breadCrumbParameter;

	private UsersGridComponent grid;

	SitesAdminsView(SiteService siteService, UserService userService, AuthzService authzService) {
		this.siteService = siteService;
		this.userService = userService;
		this.currentUserId = authzService.getCurrentUserId();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		getContent().removeAll();
		breadCrumbParameter = parameter;
		init(parameter);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return siteService.findById(breadCrumbParameter)
				.map(site -> new BreadCrumbParameter(site.getId(), site.getName()));
	}

	private void init(String siteId) {
		InviteUserComponent inviteUser = new InviteUserComponent(
			userService::getAllUsers,
			() -> siteService.findAllAdministrators(siteId)
		);

		MembershipChangerComponent membershipLayout = new MembershipChangerComponent(
			getTranslation("view.fenix-admin.sites.button.join"),
			getTranslation("view.fenix-admin.sites.button.demit"),
			() -> siteService.isCurrentUserAdminOf(siteId)
		);
		membershipLayout.addJoinButtonListener(event -> {
			siteService.addAdmin(siteId, currentUserId);
			gridReload();
			inviteUser.reload();
		});
		membershipLayout.addDemitButtonListener(event -> {
			if (siteService.findAllAdministrators(siteId).size() > 1) {
				handleExceptions(() -> siteService.removeSiteUser(siteId, currentUserId));
				gridReload();
			} else {
				showErrorNotification(getTranslation("component.administrators.error.validation.remove"));
			}
			inviteUser.reload();
			membershipLayout.loadAppropriateButton();
		});

		inviteUser.addInviteAction(event -> doInviteAction(siteId, inviteUser, membershipLayout));
		UserContextMenuFactory userContextMenuFactory = UserContextMenuFactory.builder()
			.withCurrentUserId(currentUserId)
			.withRemoveUserAction(userId -> siteService.removeSiteUser(siteId, userId))
			.withPostRemoveUserAction(userId -> {
				membershipLayout.loadAppropriateButton();
				inviteUser.reload();
			})
			.withRemoveInvitationAction(invitationId -> {
				siteService.removeInvitation(siteId, invitationId);
				gridReload();
			})
			.withResendInvitationAction(invitationId -> {
				siteService.resendInvitation(siteId, invitationId);
				gridReload();
			})
			.build();

		UserGrid.Builder userGrid = UserGrid.defaultInit(userContextMenuFactory);
		grid = UsersGridComponent.defaultInit(
			() -> siteService.findAllAdministrators(siteId),
			() -> siteService.findSiteAdminInvitations(siteId),
			userGrid);

		Site site = handleExceptions(() -> siteService.findById(siteId))
				.flatMap(identity())
				.orElseThrow(IllegalStateException::new);
		ViewHeaderLayout viewHeaderLayout = new ViewHeaderLayout(
				getTranslation("view.sites.administrators.title", site.getName()), 
				membershipLayout);
		getContent().add(viewHeaderLayout, inviteUser, grid);
	}

	private void doInviteAction(String siteId, InviteUserComponent inviteUserComponent, MembershipChangerComponent membershipLayout) {
		try {
			inviteUserComponent.getUserId().ifPresentOrElse(
				id -> siteService.inviteAdmin(siteId, id),
				() -> siteService.inviteAdmin(siteId, inviteUserComponent.getEmail())
			);
			inviteUserComponent.reload();
			membershipLayout.loadAppropriateButton();
			gridReload();
		} catch (DuplicatedInvitationError e) {
			showErrorNotification(getTranslation("invite.error.duplicate"));
		} catch (UserAlreadyHasRoleError e) {
			showErrorNotification(getTranslation("invite.error.role.own"));
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.sites.invite.error.unexpected"));
			LOG.error("Could not invite Site Administrator. ", e);
		}
	}

	private void gridReload() {
		grid.reloadGrid();
	}

}
