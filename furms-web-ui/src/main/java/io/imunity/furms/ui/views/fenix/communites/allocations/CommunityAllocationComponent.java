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

		grid.addColumn(CommunityAllocationGridModel::getSiteName)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.1"))
			.setSortable(true);
		grid.addComponentColumn(c -> new RouterLink(c.name, CommunityAllocationFormView.class, c.id))
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.2"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(CommunityAllocationGridModel::getResourceCreditName)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.3"))
			.setSortable(true);
		grid.addColumn(CommunityAllocationGridModel::getResourceTypeName)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.4"))
			.setSortable(true);
		grid.addColumn(c -> c.amount.toPlainString() + " " + c.getResourceTypeUnit())
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.5"))
			.setSortable(true)
			.setComparator(comparing(c -> c.amount));
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.6"))
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
				.getThrowable().ifPresent(t -> showErrorNotification(getTranslation(t.getMessage(), communityAllocationName)));
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
