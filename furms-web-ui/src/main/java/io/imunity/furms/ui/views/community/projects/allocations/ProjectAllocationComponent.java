/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

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
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.ui.components.*;

import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

public class ProjectAllocationComponent extends Composite<Div> {

	private final Grid<ProjectAllocationGridModel> grid;
	private final ProjectAllocationService service;
	private final String projectId;

	public ProjectAllocationComponent(ProjectAllocationService service, String projectId) {
		this.grid = createCommunityGrid();
		this.service = service;
		this.projectId = projectId;

		loadGridContent();

		Button button = new Button(getTranslation("view.community-admin.project-allocation.page.button"));
		button.setClassName("reload-disable");

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.community-admin.project-allocation.page.header"),
			new RouterGridLink(
				button,
				null,
				ProjectAllocationFormView.class,
				"projectId",
				projectId
			)
		);

		getContent().add(headerLayout, grid);
	}

	private Grid<ProjectAllocationGridModel> createCommunityGrid() {
		Grid<ProjectAllocationGridModel> grid = new SparseGrid<>(ProjectAllocationGridModel.class);

		grid.addColumn(ProjectAllocationGridModel::getSiteName)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.1"))
			.setSortable(true);
		grid.addComponentColumn(c -> new RouterLink(c.name, ProjectAllocationFormView.class, c.id))
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.2"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(ProjectAllocationGridModel::getResourceTypeName)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.4"))
			.setSortable(true);
		grid.addColumn(c -> c.amount.toPlainString() + " " + c.getResourceTypeUnit())
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.5"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.6"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ProjectAllocationGridModel communityAllocationGridModel) {
		return new GridActionsButtonLayout(
			createContextMenu(communityAllocationGridModel.id, communityAllocationGridModel.name)
		);
	}

	private Component createContextMenu(String CommunityAllocationId, String CommunityAllocation) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.community-admin.project-allocation.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ProjectAllocationFormView.class, CommunityAllocationId)
		);

		Dialog confirmDialog = createConfirmDialog(CommunityAllocationId, CommunityAllocation);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.community-admin.project-allocation.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String CommunityAllocationId, String resourceCreditName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.community-admin.project-allocation.dialog.text", resourceCreditName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> service.delete(CommunityAllocationId));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ProjectAllocationGridModel> loadServicesViewsModels() {
		return handleExceptions(() -> service.findAllWithRelatedObjects(projectId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(ProjectAllocationModelsMapper::gridMap)
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.name.toLowerCase()))
			.collect(toList());
	}
}
