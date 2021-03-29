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
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.site_agent.PendingJob;
import io.imunity.furms.domain.site_agent.SiteAgentStatus;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "site/admin/pending/requests", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.pending-requests.page.title")
public class PendingRequestsView extends FurmsViewComponent {

	public PendingRequestsView(SiteService siteService) {
		Button button = new Button(getTranslation("view.site-admin.pending-requests.page.agent.connection"));
		Label resultLabel = new Label();
		Label messageStatusLabel = new Label();
		Label correlationIdLabel = new Label();
		UI ui = UI.getCurrent();
		String siteId = getCurrentResourceId();
		button.addClickListener(event -> {
			PendingJob<SiteAgentStatus> siteAgentStatus = siteService.getSiteAgentStatus(siteId);
			resultLabel.setText("");
			messageStatusLabel.setText("START");
			correlationIdLabel.setText(siteAgentStatus.correlationId);
			siteAgentStatus.ackFuture.thenAcceptAsync(ack ->
				ui.access(() -> messageStatusLabel.setText(ack.name()))
			);
			siteAgentStatus.jobFuture.thenAcceptAsync(status -> ui.access(() -> {
				resultLabel.setText(getTranslation("view.site-admin.pending-requests.page.agent." + status.status.name()));
				messageStatusLabel.setText("DONE");
			})
			);
		});
		getContent().add(
			new VerticalLayout(
				button,
				new HorizontalLayout(new Label("Agent Status:"), resultLabel),
				new HorizontalLayout(new Label("Task Status:"), messageStatusLabel),
				new HorizontalLayout(new Label("CorrelationId:"), correlationIdLabel)
			)
		);
	}
}
