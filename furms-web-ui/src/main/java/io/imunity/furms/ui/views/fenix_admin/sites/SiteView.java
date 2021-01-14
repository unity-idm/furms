/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.sites;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;

@Route(value = "fenix/admin/site", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.site.page.title")
public class SiteView extends FurmsViewComponent {
	SiteView() {
		getContent().add(new Label("Placeholder"));
	}
}
