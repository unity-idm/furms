/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;

import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

@Route(value = "fenix/admin/sites/details", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.sites.details.title")
public class SitesDetailsView extends FurmsViewComponent {
	SitesDetailsView() {
		getContent().add(new Label("Placeholder"));
	}
}
