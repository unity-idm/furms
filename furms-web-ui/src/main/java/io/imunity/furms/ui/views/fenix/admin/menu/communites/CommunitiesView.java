/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.admin.menu.communites;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.fenix.admin.menu.FenixAdminView;

@Route(value = "fenix/admin/communities", layout = FenixAdminView.class)
@PageTitle("Communities")
public class CommunitiesView extends FurmsViewComponent {
	CommunitiesView() {
		getContent().add(new Label("Placeholder"));
	}
}
