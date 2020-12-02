/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.privileges;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;

@Route(value = "fenix/admin/privilegedUsers", layout = FenixAdminMenu.class)
@PageTitle(key = "view.privileged-users.page.title")
public class PrivilegedUsersView extends FurmsViewComponent {
	PrivilegedUsersView() {
		getContent().add(new Label("Placeholder"));
	}
}
