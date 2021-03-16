/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

@Route(value = "site/admin/pending/requests", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.pending-requests.page.title")
public class PendingRequestsView extends FurmsViewComponent {

	public PendingRequestsView(SiteService siteService) {
		Button button = new Button("Ping");
		Label label = new Label();
		UI ui = UI.getCurrent();
		button.addClickListener(event -> {
			label.setText("");
			siteService.pingAgent()
				.thenAcceptAsync(status ->
					ui.access(() -> label.setText(status.name()))
				);
		});
		getContent().add(new HorizontalLayout(button, label));
	}
}
