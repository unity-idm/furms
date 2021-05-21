/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.allocations;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.util.*;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

@Route(value = "project/admin/resource/allocations", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.resource-allocations.page.title")
public class ResourceAllocationsView extends FurmsViewComponent {
	private final Grid<ProjectAllocationGridModel> grid;
	private final ProjectAllocationService service;
	private final String projectId;
	private ProjectDataSnapshot projectDataSnapshot;


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

		grid.addComponentColumn(allocation -> {
			Icon icon = grid.isDetailsVisible(allocation) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
			return new Div(icon, new Label(allocation.getSiteName()));
		})
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
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.5"))
			.setSortable(true)
			.setComparator(comparing(c -> c.amount));
		grid.addComponentColumn(c -> {
			List<ProjectAllocationInstallation> projectAllocationInstallations = projectDataSnapshot.getAllocation(c.id);
			Optional<ProjectDeallocation> deallocation = projectDataSnapshot.getDeallocationStatus(c.id);
			if(deallocation.isPresent()) {
				int statusId = deallocation.get().status.getPersistentId();
				return getStatusLayout(
					getTranslation("view.community-admin.project-allocation.deallocation-status." + statusId),
					deallocation.flatMap(x -> x.errorMessage).map(x -> x.message).orElse(null)
				);
			}
			return projectAllocationInstallations.stream()
				.max(comparing(projectAllocationInstallationStatus -> projectAllocationInstallationStatus.status.getPersistentId()))
				.map(installation -> getStatusLayout(
					getTranslation("view.community-admin.project-allocation.status." + installation.status.getPersistentId()),
					installation.errorMessage.map(x -> x.message).orElse(null)))
				.orElseGet(HorizontalLayout::new);
		})
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.6"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.7"))
			.setTextAlign(ColumnTextAlign.END);


		grid.setItemDetailsRenderer(new ComponentRenderer<>(c -> ProjectAllocationDetailsComponentFactory
			.create(projectDataSnapshot.getAllocation(c.id))));
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		return grid;
	}

	private HorizontalLayout getStatusLayout(String status, String message) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		Text text = new Text(status);
		horizontalLayout.add(text);
		if(message != null){
			Tooltip tooltip = new Tooltip();
			Icon icon = WARNING.create();
			tooltip.attachToComponent(icon);
			tooltip.add(message);
			getContent().add(tooltip);
			horizontalLayout.add(icon);
		}

		return horizontalLayout;
	}

	private HorizontalLayout createLastColumnContent(ProjectAllocationGridModel projectAllocationGridModel) {
		return new GridActionsButtonLayout(
			createContextMenu()
		);
	}

	private Component createContextMenu() {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(getTranslation("view.user-settings.ssh-keys.grid.menu.refresh"),
			REFRESH), e -> loadGridContent());

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private void loadGridContent() {
		handleExceptions(() -> {
			projectDataSnapshot = new ProjectDataSnapshot(
				service.findAllInstallations(projectId),
				service.findAllUninstallations(projectId)
		);
			grid.setItems(loadServicesViewsModels());
		});
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
			.resourceTypeUnit(projectAllocation.resourceType.unit.getSuffix())
			.name(projectAllocation.name)
			.amount(projectAllocation.amount)
			.build();
	}

	private static class ProjectDataSnapshot {
		private final Map<String, List<ProjectAllocationInstallation>> groupedProjectAllocations;
		private final Map<String, ProjectDeallocation> deallocationsByProjectAllocationId;

		ProjectDataSnapshot(Set<ProjectAllocationInstallation> installations, Set<ProjectDeallocation> uninstallations) {
			this.groupedProjectAllocations = installations.stream()
				.collect(groupingBy(installation -> installation.projectAllocationId));
			this.deallocationsByProjectAllocationId = uninstallations.stream()
				.collect(toMap(uninstallation -> uninstallation.projectAllocationId, identity(), (x,y) -> x));
		}

		List<ProjectAllocationInstallation> getAllocation(String projectAllocationId) {
			return groupedProjectAllocations.getOrDefault(projectAllocationId, emptyList());
		}

		Optional<ProjectDeallocation> getDeallocationStatus(String projectAllocationId) {
			return Optional.ofNullable(deallocationsByProjectAllocationId.get(projectAllocationId));
		}
	}
}
