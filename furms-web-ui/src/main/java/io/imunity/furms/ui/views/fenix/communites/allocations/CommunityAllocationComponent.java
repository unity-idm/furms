/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.ui.components.AllocationDetailsComponentFactory;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.ResourceProgressBar;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.ViewHeaderLayout;

import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.SPLINE_CHART;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class CommunityAllocationComponent extends Composite<Div> {

	private final Grid<CommunityAllocationGridModel> grid;
	private final CommunityAllocationService service;
	private final CommunityId communityId;

	public CommunityAllocationComponent(CommunityAllocationService service, CommunityId communityId) {
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
				communityId.id.toString()
			)
		);

		getContent().add(headerLayout, grid);
	}

	private Grid<CommunityAllocationGridModel> createCommunityGrid() {
		Grid<CommunityAllocationGridModel> grid = new DenseGrid<>(CommunityAllocationGridModel.class);

		grid.addComponentColumn(model -> {
			Icon icon = grid.isDetailsVisible(model) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
			return new Div(icon, new Text(model.siteName));
		})
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.1"))
			.setSortable(true);
		grid.addComponentColumn(model -> new RouterLink(model.name, CommunityAllocationFormView.class, model.id.id.toString()))
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.2"))
			.setSortable(true)
			.setComparator(model -> model.name.toLowerCase())
			.setAutoWidth(true);
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
		grid.addComponentColumn(model -> new ResourceProgressBar(model.amountWithUnit.amount, model.consumed, 0))
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.8"))
			.setComparator(comparing(model -> model.consumed));
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.fenix-admin.resource-credits-allocation.grid.column.9"))
			.setTextAlign(ColumnTextAlign.END);

		grid.setItemDetailsRenderer(new ComponentRenderer<>(model ->
			AllocationDetailsComponentFactory.create(model.creationTime, model.validFrom, model.validTo)
		));

		return grid;
	}

	private HorizontalLayout createLastColumnContent(CommunityAllocationGridModel model) {
		return new GridActionsButtonLayout(
			new RouterGridLink(SPLINE_CHART, model.id.id.toString(), CommunityAllocationsDetailsView.class),
			createContextMenu(model.id, model.name)
		);
	}

	private Component createContextMenu(CommunityAllocationId communityAllocationId, String communityAllocation) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.fenix-admin.resource-credits-allocation.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(CommunityAllocationFormView.class, communityAllocationId.id.toString())
		);

		Dialog confirmDialog = createConfirmDialog(communityAllocationId, communityAllocation);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.fenix-admin.resource-credits-allocation.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(CommunityAllocationId communityAllocationId, String communityAllocationName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.fenix-admin.resource-credits-allocation.dialog.text", communityAllocationName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			getResultOrException(() -> service.delete(communityAllocationId))
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
