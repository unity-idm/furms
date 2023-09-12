/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.allocations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.AllocationDetailsComponentFactory;
import io.imunity.furms.ui.components.ResourceProgressBar;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.StatusLayout;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.project_allocation.ProjectAllocationDataSnapshot;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
import static com.vaadin.flow.component.icon.VaadinIcon.SPLINE_CHART;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "project/admin/resource/allocations", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.resource-allocations.page.title")
public class ResourceAllocationsView extends FurmsViewComponent {
	private final Grid<ProjectAllocationGridModel> grid;
	private final ProjectAllocationService service;
	private final ProjectId projectId;
	private final AlarmService alarmService;
	private ProjectAllocationDataSnapshot projectDataSnapshot;


	ResourceAllocationsView(ProjectAllocationService service, AlarmService alarmService) {
		this.service = service;
		this.alarmService = alarmService;
		this.grid = createCommunityGrid();
		this.projectId = new ProjectId(getCurrentResourceId());

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.project-admin.resource-allocations.page.header")
		);

		loadGridContent();
		getContent().add(headerLayout, grid);
	}

	private Grid<ProjectAllocationGridModel> createCommunityGrid() {
		Grid<ProjectAllocationGridModel> grid = new DenseGrid<>(ProjectAllocationGridModel.class);

		grid.addComponentColumn(allocation -> {
			Icon icon = grid.isDetailsVisible(allocation) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
			return new Div(icon, new Label(allocation.siteName));
		})
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.1"))
			.setSortable(true);
		grid.addColumn(model -> model.name)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.2"))
			.setSortable(true)
			.setComparator(model -> model.name.toLowerCase());
		grid.addColumn(model -> model.resourceTypeName)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.3"))
			.setSortable(true);
		grid.addColumn(model -> model.amountWithUnit)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.4"))
			.setSortable(true)
			.setComparator(comparing(model -> model.amountWithUnit.amount));
		grid.addColumn(model -> model.consumedWithUnit)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.5"))
			.setSortable(true)
			.setComparator(comparing(model -> model.consumedWithUnit.amount));
		grid.addColumn(model -> model.remainingWithUnit)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.6"))
			.setSortable(true)
			.setComparator(comparing(model -> model.remainingWithUnit.amount));
		grid.addComponentColumn(c -> {
			Optional<ProjectAllocationInstallation> projectAllocationInstallations = projectDataSnapshot.getAllocation(c.id);
			Optional<ProjectDeallocation> deallocation = projectDataSnapshot.getDeallocationStatus(c.id);
			if(deallocation.isPresent()) {
				int statusId = deallocation.get().status.getPersistentId();
				return new StatusLayout(
					getTranslation("view.community-admin.project-allocation.deallocation-status." + statusId),
					deallocation.flatMap(x -> x.errorMessage).map(x -> x.message).orElse(null)
				);
			}
			return projectAllocationInstallations
				.map(installation -> {
					final int statusId = installation.status.getPersistentId();
					return new StatusLayout(
						getTranslation("view.community-admin.project-allocation.status." + statusId),
						installation.errorMessage.map(x -> x.message).orElse(null));
				}
				).orElseGet(StatusLayout::new);
		})
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.7"))
			.setSortable(true);
		grid.addComponentColumn(model ->
			new ResourceProgressBar(
				model.amountWithUnit.amount,
				model.consumedWithUnit.amount,
				projectDataSnapshot.getAlarmThreshold(model.id)
			)
		)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.8"))
			.setTextAlign(ColumnTextAlign.CENTER);

		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.9"))
			.setTextAlign(ColumnTextAlign.END);


		grid.setItemDetailsRenderer(new ComponentRenderer<>(c -> AllocationDetailsComponentFactory
			.create(projectDataSnapshot.getChunks(c.id), c.amountWithUnit.unit)));
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ProjectAllocationGridModel projectAllocationGridModel) {
		return new GridActionsButtonLayout(
			new RouterGridLink(SPLINE_CHART, projectAllocationGridModel.id.id.toString(), ResourceAllocationsDetailsView.class),
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
			projectDataSnapshot = new ProjectAllocationDataSnapshot(
				service.findAllInstallations(projectId),
				service.findAllUninstallations(projectId),
				service.findAllChunks(projectId),
				alarmService.findAll(projectId)
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
			.resourceTypeUnit(projectAllocation.resourceType.unit)
			.name(projectAllocation.name)
			.amount(projectAllocation.amount)
			.consumed(projectAllocation.consumed)
			.build();
	}
}
