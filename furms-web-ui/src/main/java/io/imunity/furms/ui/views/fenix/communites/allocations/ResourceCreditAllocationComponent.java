/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.resource_credit_allocation.ResourceCreditAllocationService;
import io.imunity.furms.ui.components.*;

import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class ResourceCreditAllocationComponent extends Composite<Div> {

	private final Grid<ResourceCreditAllocationGridModel> grid;
	private final ResourceCreditAllocationService service;
	private final String communityId;

	public ResourceCreditAllocationComponent(ResourceCreditAllocationService service, String communityId) {
		this.grid = createCommunityGrid();
		this.service = service;
		this.communityId = communityId;

		loadGridContent();

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.fenix-admin.resource-credits-allocation.page.header"),
			new RouterGridLink(
				new Button(getTranslation("view.fenix-admin.resource-credits-allocation.page.button")),
				null,
				ResourceCreditAllocationFormView.class,
				"communityId",
				communityId
			)
		);

		getContent().add(headerLayout, grid);
	}

	private Grid<ResourceCreditAllocationGridModel> createCommunityGrid() {
		Grid<ResourceCreditAllocationGridModel> grid = new SparseGrid<>(ResourceCreditAllocationGridModel.class);

		grid.addColumn(ResourceCreditAllocationGridModel::getSiteName)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.1"))
			.setSortable(true);
		grid.addComponentColumn(c -> new RouterLink(c.name, ResourceCreditAllocationFormView.class, c.id))
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.2"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(ResourceCreditAllocationGridModel::getResourceCreditName)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.3"))
			.setSortable(true);
		grid.addColumn(ResourceCreditAllocationGridModel::getResourceTypeName)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.4"))
			.setSortable(true);
		grid.addColumn(c -> c.amount.toPlainString() + " " + c.getResourceTypeUnit())
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.5"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.6"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ResourceCreditAllocationGridModel resourceCreditAllocationGridModel) {
		return new GridActionsButtonLayout(
			createContextMenu(resourceCreditAllocationGridModel.id, resourceCreditAllocationGridModel.name)
		);
	}

	private Component createContextMenu(String resourceCreditAllocationId, String resourceCreditAllocation) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.fenix-admin.resource-credits-allocation.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ResourceCreditAllocationFormView.class, resourceCreditAllocationId)
		);

		Dialog confirmDialog = createConfirmDialog(resourceCreditAllocationId, resourceCreditAllocation);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.fenix-admin.resource-credits-allocation.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String resourceCreditAllocationId, String resourceCreditName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.fenix-admin.resource-credits-allocation.dialog.text", resourceCreditName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> service.delete(resourceCreditAllocationId));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ResourceCreditAllocationGridModel> loadServicesViewsModels() {
		return handleExceptions(() -> service.findAllWithRelatedObjects(communityId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(ResourceCreditAllocationModelsMapper::gridMap)
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.name.toLowerCase()))
			.collect(toList());
	}
}
