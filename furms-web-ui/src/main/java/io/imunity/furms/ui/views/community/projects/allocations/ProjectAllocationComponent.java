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
import io.imunity.furms.api.project_allocation_installation.ProjectAllocationInstallationService;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.ui.components.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class ProjectAllocationComponent extends Composite<Div> {

	private final Grid<ProjectAllocationGridModel> grid;
	private final ProjectAllocationService service;
	private final String communityId;
	private final String projectId;

	public ProjectAllocationComponent(ProjectAllocationService service, ProjectAllocationInstallationService projectAllocationInstallationService, String projectId) {
		this.communityId = getCurrentResourceId();
		this.service = service;
		this.projectId = projectId;
		Map<String, List<ProjectAllocationInstallation>> groupedProjectAllocations = projectAllocationInstallationService.findAll(communityId, projectId).stream()
			.collect(groupingBy(x -> x.projectAllocationId));
		this.grid = createCommunityGrid(groupedProjectAllocations);

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

	private Grid<ProjectAllocationGridModel> createCommunityGrid(Map<String, List<ProjectAllocationInstallation>> groupedProjectAllocations) {
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
		grid.addColumn(c -> groupedProjectAllocations.get(c.id).get(0).status)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.5"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.6"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ProjectAllocationGridModel projectAllocationGridModel) {
		return new GridActionsButtonLayout(
			createContextMenu(projectAllocationGridModel.id, projectAllocationGridModel.name)
		);
	}

	private Component createContextMenu(String projectAllocationId, String projectAllocation) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
				getTranslation("view.community-admin.project-allocation.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ProjectAllocationFormView.class, projectAllocationId)
		);

		Dialog confirmDialog = createConfirmDialog(projectAllocationId, projectAllocation);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.community-admin.project-allocation.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String projectAllocationId, String projectAllocationName) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.community-admin.project-allocation.dialog.text", projectAllocationName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> service.delete(communityId, projectAllocationId));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ProjectAllocationGridModel> loadServicesViewsModels() {
		return handleExceptions(() -> service.findAllWithRelatedObjects(communityId, projectId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(ProjectAllocationModelsMapper::gridMap)
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.name.toLowerCase()))
			.collect(toList());
	}
}
