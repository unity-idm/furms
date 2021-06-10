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
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.ui.components.*;

import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class CommunityAllocationComponent extends Composite<Div> {

	private final Grid<CommunityAllocationGridModel> grid;
	private final CommunityAllocationService service;
	private final String communityId;

	public CommunityAllocationComponent(CommunityAllocationService service, String communityId) {
		this.grid = createCommunityGrid();
		this.service = service;
		this.communityId = communityId;

		loadGridContent();

		Button button = new Button(getTranslation("view.fenix-admin.resource-credits-allocation.page.button"));
		button.setClassName("reload-disable");

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.fenix-admin.resource-credits-allocation.page.header"),
			new RouterGridLink(
				button,
				null,
				CommunityAllocationFormView.class,
				"communityId",
				communityId
			)
		);

		getContent().add(headerLayout, grid);
	}

	private Grid<CommunityAllocationGridModel> createCommunityGrid() {
		Grid<CommunityAllocationGridModel> grid = new SparseGrid<>(CommunityAllocationGridModel.class);

		grid.addColumn(model -> model.siteName)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.1"))
			.setSortable(true);
		grid.addComponentColumn(model -> new RouterLink(model.name, CommunityAllocationFormView.class, model.id))
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.2"))
			.setSortable(true)
			.setComparator(model -> model.name.toLowerCase());
		grid.addColumn(model -> model.resourceCreditName)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.3"))
			.setSortable(true);
		grid.addColumn(model -> model.resourceTypeName)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.4"))
			.setSortable(true);
		grid.addColumn(model -> model.amountWithUnit)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.5"))
			.setSortable(true)
			.setComparator(comparing(model -> model.amountWithUnit.amount));
		grid.addColumn(model -> model.distributedWithUnit)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.6"))
			.setSortable(true)
			.setComparator(comparing(model -> model.distributedWithUnit.amount));
		grid.addColumn(model -> model.remainingWithUnit)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.7"))
			.setSortable(true)
			.setComparator(comparing(model -> model.remainingWithUnit.amount));
		grid.addComponentColumn(model -> {
			double value = model.consumed
				.divide(model.amountWithUnit.amount, 4, HALF_UP)
				.doubleValue();
			return new FurmsProgressBar(value);
		})
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.8"))
			.setTextAlign(ColumnTextAlign.END);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.9"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(CommunityAllocationGridModel communityAllocationGridModel) {
		return new GridActionsButtonLayout(
			createContextMenu(communityAllocationGridModel.id, communityAllocationGridModel.name)
		);
	}

	private Component createContextMenu(String CommunityAllocationId, String CommunityAllocation) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.fenix-admin.resource-credits-allocation.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(CommunityAllocationFormView.class, CommunityAllocationId)
		);

		Dialog confirmDialog = createConfirmDialog(CommunityAllocationId, CommunityAllocation);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.fenix-admin.resource-credits-allocation.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String CommunityAllocationId, String communityAllocationName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.fenix-admin.resource-credits-allocation.dialog.text", communityAllocationName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			getResultOrException(() -> service.delete(CommunityAllocationId))
				.getException().ifPresent(t -> showErrorNotification(getTranslation(t.getMessage(), communityAllocationName)));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<CommunityAllocationGridModel> loadServicesViewsModels() {
		return handleExceptions(() -> service.findAllWithRelatedObjects(communityId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(CommunityAllocationGridModelMapper::gridMap)
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.name.toLowerCase()))
			.collect(toList());
	}
}
