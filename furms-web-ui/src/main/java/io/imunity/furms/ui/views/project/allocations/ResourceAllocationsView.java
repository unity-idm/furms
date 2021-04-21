/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.allocations;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

@Route(value = "project/admin/resource/allocations", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.resource-allocations.page.title")
public class ResourceAllocationsView extends FurmsViewComponent {
	private final Grid<ProjectAllocationGridModel> grid;
	private final ProjectAllocationService service;
	private final String projectId;

	ResourceAllocationsView(ProjectAllocationService service) {
		this.service = service;
		this.grid = createCommunityGrid();
		this.projectId = getCurrentResourceId();

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.project-admin.resource-allocations.page.header")
		);

		loadGridContent();
		getContent().add(headerLayout, grid);
	}

	private Grid<ProjectAllocationGridModel> createCommunityGrid() {
		Grid<ProjectAllocationGridModel> grid = new SparseGrid<>(ProjectAllocationGridModel.class);

		grid.addColumn(ProjectAllocationGridModel::getSiteName)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.1"))
			.setSortable(true);
		grid.addColumn(c -> c.name)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.2"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(ProjectAllocationGridModel::getResourceTypeName)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.3"))
			.setSortable(true);
		grid.addColumn(c -> c.amount.toPlainString() + " " + c.getResourceTypeUnit())
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.4"))
			.setSortable(true);
		return grid;
	}

	private void loadGridContent() {
		grid.setItems(loadServicesViewsModels());
	}

	private List<ProjectAllocationGridModel> loadServicesViewsModels() {
		return handleExceptions(() -> service.findAllWithRelatedObjects(projectId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(this::gridMap)
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.name.toLowerCase()))
			.collect(toList());
	}

	private ProjectAllocationGridModel gridMap(ProjectAllocationResolved projectAllocation) {
		return ProjectAllocationGridModel.builder()
			.id(projectAllocation.id)
			.projectId(projectAllocation.projectId)
			.siteName(projectAllocation.site.getName())
			.resourceTypeName(projectAllocation.resourceType.name)
			.resourceTypeUnit(projectAllocation.resourceType.unit.name())
			.name(projectAllocation.name)
			.amount(projectAllocation.amount)
			.build();
	}
}
