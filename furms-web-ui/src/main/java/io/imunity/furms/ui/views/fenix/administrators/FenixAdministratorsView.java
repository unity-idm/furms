/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.administrators;

import com.vaadin.flow.router.Route;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.administrators.AdministratorsView;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.util.List;

@Route(value = "fenix/admin/administrators", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.administrators.page.title")
public class FenixAdministratorsView extends AdministratorsView {

	FenixAdministratorsView(UsersDAO usersDAO) {
		super(usersDAO);
	}

	@Override
	protected List<User> fetchUsers() {
		return usersDAO.getAdminUsers();
	}

	@Override
	protected void addUser(String id) {
		usersDAO.addFenixAdminRole(id);
	}

	@Override
	protected void removeUser(String id) {
		usersDAO.removeFenixAdminRole(id);
	}

}
