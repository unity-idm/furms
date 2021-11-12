/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.connection;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.site_agent_pending_message.SiteAgentPendingMessageService;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.administrators.SearchLayout;
import io.imunity.furms.ui.user_context.UIContext;
import io.imunity.furms.ui.views.site.SiteAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

@Route(value = "site/admin/pending/requests", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.pending-requests.page.title")
public class PendingRequestsView extends FurmsViewComponent {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final SiteAgentPendingMessageService siteAgentPendingMessageService;

	private final ZoneId browserZoneId;
	private final SiteId siteId;
	private final Grid<PendingMessageGridModel> grid;
	private final SearchLayout searchLayout;

	public PendingRequestsView(SiteAgentPendingMessageService siteAgentPendingMessageService) {
		UI ui = UI.getCurrent();
		this.siteId = new SiteId(getCurrentResourceId());
		this.siteAgentPendingMessageService = siteAgentPendingMessageService;
		this.searchLayout = new SearchLayout();
		this.browserZoneId = UIContext.getCurrent().getZone();
		Map<CorrelationId, Checkbox> checkboxes = new HashMap<>();
		grid = createPendingMessagesGrid(checkboxes, createMainContextMenu(checkboxes));

		VerticalLayout siteConnectionLayout = createSiteConnectionLayout(ui, siteId);

		ViewHeaderLayout viewHeaderLayout = new ViewHeaderLayout(
			getTranslation("view.site-admin.pending-requests.page.header")
		);

		getContent().add(viewHeaderLayout, siteConnectionLayout, searchLayout, grid);
	}

	private boolean rowContains(PendingMessageGridModel row, String value, SearchLayout searchLayout) {
		String lowerCaseValue = value.toLowerCase();
		return searchLayout.getSearchText().isEmpty()
			|| row.operationType.toLowerCase().contains(lowerCaseValue)
			|| row.status.toLowerCase().contains(lowerCaseValue)
			|| String.valueOf(row.sentAt).contains(lowerCaseValue)
			|| String.valueOf(row.ackAt).contains(lowerCaseValue)
			|| String.valueOf(row.retryAmount).contains(lowerCaseValue);
	}

	private Component createMainContextMenu(Map<CorrelationId, Checkbox> checkboxes) {
		GridActionMenu contextMenu = new GridActionMenu();
		Dialog retryConfirmDialog = createMainConfirmDialog(
			checkboxes,
			siteAgentPendingMessageService::retry,
			getTranslation("view.site-admin.pending-requests.page.dialog.retry.confirmation")
		);
		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.pending-requests.page.context-menu.retry"), REFRESH),
			event -> retryConfirmDialog.open()
		);

		Dialog deleteConfirmDialog = createMainConfirmDialog(
			checkboxes,
			siteAgentPendingMessageService::delete,
			getTranslation("view.site-admin.pending-requests.page.dialog.delete.confirmation"));
		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.pending-requests.page.context-menu.delete"), TRASH),
			event -> deleteConfirmDialog.open()
		);
		return contextMenu.getTarget();
	}

	private Dialog createMainConfirmDialog(Map<CorrelationId, Checkbox> checkboxes, Consumer<CorrelationId> consumer, String message) {
		FurmsDialog furmsDialog = new FurmsDialog(message);
		furmsDialog.addConfirmButtonClickListener(event -> {
			try {
				checkboxes.entrySet().stream()
					.filter(x -> x.getValue().getValue())
					.forEach(x -> consumer.accept(x.getKey()));
			} catch (Exception e){
				LOG.warn("Error: ", e);
				showErrorNotification(getTranslation("base.error.message"));
			}
			loadGrid();
		});
		return furmsDialog;
	}

	private Grid<PendingMessageGridModel> createPendingMessagesGrid(Map<CorrelationId, Checkbox> checkboxes, Component mainContextMenu) {
		Grid<PendingMessageGridModel> grid;
		grid = new DenseGrid<>(PendingMessageGridModel.class);

		grid.addComponentColumn(pendingMessageGridModel -> {
			Checkbox checkbox = new Checkbox();
			checkboxes.put(pendingMessageGridModel.id ,checkbox);
			return new HorizontalLayout(checkbox, new Label(pendingMessageGridModel.operationType));
		}).setHeader(new HorizontalLayout(mainContextMenu, new Label(getTranslation("view.site-admin.pending-requests.page.grid.1"))));
		grid.addColumn(x -> x.status)
			.setHeader(getTranslation("view.site-admin.pending-requests.page.grid.2"))
			.setSortable(true);
		grid.addColumn(x -> x.sentAt.format(dateTimeFormatter))
			.setHeader(getTranslation("view.site-admin.pending-requests.page.grid.3"))
			.setSortable(true);
		grid.addColumn(x -> x.ackAt.format(dateTimeFormatter))
			.setHeader(getTranslation("view.site-admin.pending-requests.page.grid.4"))
			.setSortable(true);
		grid.addColumn(x -> x.retryAmount)
			.setHeader(getTranslation("view.site-admin.pending-requests.page.grid.5"))
			.setSortable(true);
		grid.addComponentColumn(x -> createContextMenu(x.id))
			.setHeader(getTranslation("view.site-admin.pending-requests.page.grid.6"))
			.setTextAlign(ColumnTextAlign.END);

		grid.setItemDetailsRenderer(new ComponentRenderer<>(data -> {
			Div div = new Div();
			div.setId("view-detail");
			div.addClassNames("center-rows", "row-details");
//			Paragraph p = new Paragraph("this text\nhas\nsome line breaks");
//			p.getElement().getStyle().set("white-space", "pre");
			return div;
		}));
		return grid;
	}

	private Component createContextMenu(CorrelationId id) {
		GridActionMenu contextMenu = new GridActionMenu();

		Dialog retryConfirmDialog = createConfirmDialog(
			() -> siteAgentPendingMessageService.retry(id),
			getTranslation("view.site-admin.pending-requests.page.grid.dialog.retry.confirmation")
		);
		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.pending-requests.page.grid.context-menu.retry"), REFRESH),
			event -> retryConfirmDialog.open()
		);

		Dialog deleteConfirmDialog = createConfirmDialog(
			() -> siteAgentPendingMessageService.delete(id),
			getTranslation("view.site-admin.pending-requests.page.grid.dialog.delete.confirmation")
		);
		contextMenu.addItem(new MenuButton(
				getTranslation("view.site-admin.pending-requests.page.grid.context-menu.delete"), TRASH),
			event -> deleteConfirmDialog.open()
		);

		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(Runnable operation, String message) {
		FurmsDialog furmsDialog = new FurmsDialog(message);
		furmsDialog.addConfirmButtonClickListener(event -> {
			try {
				operation.run();
			} catch (Exception e){
				LOG.warn("Error ", e);
				showErrorNotification(getTranslation("base.error.message"));
			}
			loadGrid();
		});
		return furmsDialog;
	}

	private void loadGrid() {
		Set<PendingMessageGridModel> collect = siteAgentPendingMessageService.findAll(siteId)
			.stream()
			.map(message ->
				PendingMessageGridModel.builder()
					.id(message.correlationId)
					.operationType(message.jsonContent.substring(0,20))
					.status(message.utcAckAt == null ?
						getTranslation("view.site-admin.pending-requests.page.grid.status.pending") :
						getTranslation("view.site-admin.pending-requests.page.grid.status.ack")
					)
					.sentAt(convertToZoneTime(message.utcSentAt, browserZoneId).toLocalDateTime())
					.ackAt(convertToZoneTime(message.utcAckAt, browserZoneId).toLocalDateTime())
					.retryAmount(message.retryAmount)
					.json(message.jsonContent)
					.build()
			)
			.filter(model -> rowContains(model, searchLayout.getSearchText(), searchLayout))
			.collect(Collectors.toSet());
		grid.setItems(collect);
	}

	private VerticalLayout createSiteConnectionLayout(UI ui, SiteId siteId) {
		Button button = new Button(getTranslation("view.site-admin.pending-requests.page.agent.connection"));
		Label resultLabel = new Label();

		ProgressBar progressBar = new ProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		progressBar.setWidth("10em");

		button.addClickListener(event -> handleExceptions(() -> siteAgentPendingMessageService.getSiteAgentStatus(siteId.id))
			.ifPresent(siteAgentStatus -> {
				resultLabel.setText("");
				progressBar.setVisible(true);
				siteAgentStatus.jobFuture.thenAcceptAsync(status -> ui.access(() -> {
					resultLabel.setText(getTranslation("view.site-admin.pending-requests.page.agent." + status.status.name()));
					progressBar.setVisible(false);
				}));
			}));

		VerticalLayout verticalLayout = new VerticalLayout(
			button,
			new HorizontalLayout(
				new Label(getTranslation("view.site-admin.pending-requests.page.agent.status")),
				progressBar,
				resultLabel
			)
		);
		verticalLayout.setPadding(false);
		return verticalLayout;
	}
}
