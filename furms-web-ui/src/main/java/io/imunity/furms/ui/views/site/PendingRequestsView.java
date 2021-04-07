/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;

@Route(value = "site/admin/pending/requests", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.pending-requests.page.title")
public class PendingRequestsView extends FurmsViewComponent {

	public PendingRequestsView(SiteService siteService) {
		Button button = new Button(getTranslation("view.site-admin.pending-requests.page.agent.connection"));
		Label resultLabel = new Label();
		UI ui = UI.getCurrent();
		String siteId = getCurrentResourceId();

		ProgressBar progressBar = new ProgressBar();

		button.addClickListener(event -> {
			handleExceptions(() -> siteService.getSiteAgentStatus(siteId))
				.ifPresent(siteAgentStatus -> {
					resultLabel.setText("");
					progressBar.setIndeterminate(true);
					siteAgentStatus.jobFuture.thenAcceptAsync(status -> ui.access(() -> {
						resultLabel.setText(getTranslation("view.site-admin.pending-requests.page.agent." + status.status.name()));
						progressBar.setValue(progressBar.getMax());
					}));
				});
		});
		getContent().add(
			new VerticalLayout(
				new HorizontalLayout(button, progressBar),
				new HorizontalLayout(new Label("Agent Status:"), resultLabel)
			)
		);
	}
}
