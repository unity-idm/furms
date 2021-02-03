/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.administrators;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.users.UserService;
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

@Route(value = "fenix/admin/administrators", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.administrators.page.title")
public class FenixAdministratorsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UserService userService;

	private final AdministratorsGridComponent grid;

	FenixAdministratorsView(UserService userService) {
		this.userService = userService;
		this.grid = new AdministratorsGridComponent(userService::getFenixAdmins, userService::removeFenixAdminRole);

		addHeader();
		getContent().add(grid);
	}

	private void addHeader() {
		InviteUserComponent inviteUser = new InviteUserComponent();
		inviteUser.addInviteAction(event -> doInviteAction(inviteUser.getEmail()));

		getContent().add(new ViewHeaderLayout(getTranslation("view.fenix-admin.header"), inviteUser));
	}

	private void doInviteAction(TextField email) {
		try {
			userService.invite(email.getValue());
			email.clear();
			grid.reloadGrid();
		} catch (IllegalArgumentException e) {
			email.setErrorMessage(getTranslation("view.fenix-admin.invite.error.validation.field.invite"));
			email.setInvalid(true);
			LOG.error("Could not invite user. ", e);
		} catch (RuntimeException e) {
			showErrorNotification(getTranslation("view.fenix-admin.invite.error.unexpected"));
			LOG.error("Could not invite user. ", e);
		}
	}

}
