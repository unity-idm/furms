/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.admin.menu.privileges;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.fenix.admin.menu.FenixAdminView;

@Route(value = "fenix/admin/privilegedUsers", layout = FenixAdminView.class)
@PageTitle("Privileged Users")
public class PrivilegedUsersView extends FurmsViewComponent {
	PrivilegedUsersView() {
		getContent().add(new Label("Placeholder"));
	}
}
