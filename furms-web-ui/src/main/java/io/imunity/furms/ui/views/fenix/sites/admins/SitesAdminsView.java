/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites.admins;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.InviteUserComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.AdministratorsGridComponent;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;

@Route(value = "fenix/admin/sites/details", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.sites.details.title")
public class SitesAdminsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteService siteService;

	private AdministratorsGridComponent grid;
	private String siteId;

	SitesAdminsView(SiteService siteService) {
		this.siteService = siteService;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		init(parameter);
	}

	private void init(String siteId) {
		this.grid = new AdministratorsGridComponent(
				() -> siteService.findAllAdmins(siteId),
				userId -> siteService.removeAdmin(siteId, userId));
		this.siteId = siteId;

		addHeader();
		getContent().add(grid);
	}

	private void addHeader() {
		InviteUserComponent inviteUser = new InviteUserComponent();
		inviteUser.addInviteAction(event -> doInviteAction(inviteUser.getEmail()));

		getContent().add(new ViewHeaderLayout(getTranslation("view.sites.administrators.title"), inviteUser));
	}

	private void doInviteAction(TextField email) {
		try {
			siteService.inviteAdmin(siteId, email.getValue());
			email.clear();
			grid.reloadGrid();
		} catch (IllegalArgumentException e) {
			email.setErrorMessage(getTranslation("view.sites.invite.error.validation.field.invite"));
			email.setInvalid(true);
			LOG.error("Could not invite Site Administrator. ", e);
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.sites.invite.error.unexpected"));
			LOG.error("Could not invite Site Administrator. ", e);
		}
	}

}
