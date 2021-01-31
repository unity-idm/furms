/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites.admins;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.administrators.AdministratorsView;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.util.List;

@Route(value = "fenix/admin/sites/details", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.sites.details.title")
public class SitesAdminsView extends AdministratorsView {

	private final SiteService siteService;

	private String siteId;

	SitesAdminsView(UsersDAO usersDAO, SiteService siteService) {
		super(usersDAO, false);
		this.siteService = siteService;
	}

	@Override
	protected List<User> fetchUsers() {
		return siteService.findAllAdmins(siteId);
	}

	@Override
	protected void addUser(String id) {
		siteService.addAdmin(siteId, id);
	}

	@Override
	protected void removeUser(String id) {
		siteService.removeAdmin(siteId, id);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		siteId = parameter;
		render();
	}

}
