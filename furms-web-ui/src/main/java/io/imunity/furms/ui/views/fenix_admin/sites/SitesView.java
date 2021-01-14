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

import static io.imunity.furms.domain.constant.RoutesConst.FENIX_ADMIN_LANDING_PAGE;

@Route(value = FENIX_ADMIN_LANDING_PAGE, layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.sites.page.title")
public class SitesView extends FurmsViewComponent {
	SitesView() {
		getContent().add(new Label("Placeholder"));
	}
}
