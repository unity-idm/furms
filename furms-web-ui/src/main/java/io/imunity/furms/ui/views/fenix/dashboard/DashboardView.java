/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.dashboard;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

@Route(value = "fenix/admin/dashboard", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.dashboard.page.title")
public class DashboardView extends FurmsViewComponent {
	DashboardView() {
		getContent().add(new Label("Placeholder"));
	}
}
