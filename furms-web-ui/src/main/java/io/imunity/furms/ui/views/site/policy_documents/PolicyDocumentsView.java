/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.policy_documents;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.SiteId;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsLandingViewComponent;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.utils.CommonExceptionsHandler;
import io.imunity.furms.ui.views.site.SiteAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.icon.VaadinIcon.USERS;
import static io.imunity.furms.domain.constant.RoutesConst.SITE_BASE_LANDING_PAGE;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = SITE_BASE_LANDING_PAGE, layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.policy-documents.page.title")
public class PolicyDocumentsView extends FurmsLandingViewComponent {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final PolicyDocumentService policyDocumentService;
	private final Class<? extends FurmsViewComponent> acceptanceView;
	private final boolean editable;

	private Grid<PolicyDocumentGridModel> grid;

	@Autowired
	PolicyDocumentsView(PolicyDocumentService policyDocumentService) {
		this.policyDocumentService = policyDocumentService;
		this.acceptanceView = PolicyDocumentAcceptanceView.class;
		this.editable = true;
	}

	protected PolicyDocumentsView(PolicyDocumentService policyDocumentService, Class<? extends FurmsViewComponent> acceptanceView) {
		this.policyDocumentService = policyDocumentService;
		this.acceptanceView = acceptanceView;
		this.editable = false;
	}

	private void loadPageContent() {
		grid = createPolicyDocumentGrid();

		loadGridContent();
		ViewHeaderLayout viewHeaderLayout = editable ?
			new ViewHeaderLayout(getTranslation("view.site-admin.policy-documents.page.header"), createAddButton()):
			new ViewHeaderLayout(getTranslation("view.site-admin.policy-documents.page.header"));
		getContent().add(viewHeaderLayout, grid);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.site-admin.policy-documents.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(PolicyDocumentFormView.class));
		return addButton;
	}

	private void loadGridContent() {
		grid.setItems(loadPolicyDocumentsGridModels());
	}

	private List<PolicyDocumentGridModel> loadPolicyDocumentsGridModels() {
		SiteId siteId = new SiteId(getCurrentResourceId());
		return handleExceptions(() -> policyDocumentService.findAllBySiteId(siteId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(document -> new PolicyDocumentGridModel(document.id, document.siteId, document.name, document.workflow))
			.sorted(comparing(projectViewModel -> projectViewModel.name.toLowerCase()))
			.collect(toList());
	}

	private Grid<PolicyDocumentGridModel> createPolicyDocumentGrid() {
		Grid<PolicyDocumentGridModel> grid = new DenseGrid<>(PolicyDocumentGridModel.class);

		grid.addComponentColumn(model -> {
			if(editable)
				return new RouterLink(model.name, PolicyDocumentFormView.class, model.id.id.toString());
			else
				return new Label(model.name);
		})
			.setHeader(getTranslation("view.site-admin.policy-documents.grid.1"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(model -> getTranslation("view.site-admin.policy-documents.workflow." + model.workflow.getPersistentId()))
			.setHeader(getTranslation("view.site-admin.policy-documents.grid.2"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.site-admin.policy-documents.grid.3"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(PolicyDocumentGridModel model) {
		if(!editable)
			return createLastColumnWithoutContextMenu(model);
		Component contextMenu = createContextMenu(model.id, model.name, model.siteId);
		return new GridActionsButtonLayout(new RouterGridLink(USERS, model.id.id.toString(), acceptanceView), contextMenu);
	}

	private HorizontalLayout createLastColumnWithoutContextMenu(PolicyDocumentGridModel model) {
			return new GridActionsButtonLayout(new RouterGridLink(USERS, model.id.id.toString(), acceptanceView));
	}

	private Component createContextMenu(PolicyId policyDocumentId, String policyDocumentName, SiteId siteId) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
			getTranslation("view.site-admin.policy-documents.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(PolicyDocumentFormView.class, policyDocumentId.id.toString())
		);

		Dialog confirmDialog = createConfirmDialog(policyDocumentId, policyDocumentName, siteId);

		contextMenu.addItem(new MenuButton(
			getTranslation("view.site-admin.policy-documents.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(PolicyId policyDocumentId, String policyDocumentName, SiteId siteId) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.policy-documents.dialog.text", policyDocumentName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			try {
				policyDocumentService.delete(siteId, policyDocumentId);
				loadGridContent();
			} catch (RuntimeException e) {
				boolean handled = CommonExceptionsHandler.showExceptionBasedNotificationError(e);
				if(!handled)
					LOG.error("Could not remove policy.");
			}
		});
		return furmsDialog;
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		getContent().removeAll();
		loadPageContent();
	}
}
