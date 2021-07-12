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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsLandingViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

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
	private final PolicyDocumentService policyDocumentService;

	private Grid<PolicyDocumentGridModel> grid;

	PolicyDocumentsView(PolicyDocumentService policyDocumentService) {
		this.policyDocumentService = policyDocumentService;
	}

	private void loadPageContent() {
		Button addButton = createAddButton();
		grid = createPolicyDocumentGrid();

		loadGridContent();

		getContent().add(createHeaderLayout(addButton), grid);
	}

	private HorizontalLayout createHeaderLayout(Button addButton) {
		return new ViewHeaderLayout(getTranslation("view.site-admin.policy-documents.page.header"), addButton);
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
		String siteId = getCurrentResourceId();
		return handleExceptions(() -> policyDocumentService.findAllBySiteId(siteId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(document -> new PolicyDocumentGridModel(document.id, document.siteId, document.name, document.workflow))
			.sorted(comparing(projectViewModel -> projectViewModel.name.toLowerCase()))
			.collect(toList());
	}

	private Grid<PolicyDocumentGridModel> createPolicyDocumentGrid() {
		Grid<PolicyDocumentGridModel> grid = new SparseGrid<>(PolicyDocumentGridModel.class);

		grid.addComponentColumn(model -> new RouterLink(model.name, PolicyDocumentFormView.class, model.id.id.toString()))
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
		Component contextMenu = createContextMenu(model.id, model.name, model.siteId);
		if(model.workflow.equals(PolicyWorkflow.PAPER_BASED))
			return new GridActionsButtonLayout(new RouterGridLink(USERS, model.id.id.toString(), PolicyDocumentAcceptanceView.class), contextMenu);
		else
			return new GridActionsButtonLayout(contextMenu);
	}

	private Component createContextMenu(PolicyId policyDocumentId, String policyDocumentName, String siteId) {
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

	private Dialog createConfirmDialog(PolicyId policyDocumentId, String policyDocumentName, String siteId) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.policy-documents.dialog.text", policyDocumentName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> policyDocumentService.delete(siteId, policyDocumentId));
			loadGridContent();
		});
		return furmsDialog;
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		getContent().removeAll();
		loadPageContent();
	}
}
