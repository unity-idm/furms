/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

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
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.project_allocation.ProjectAllocationDataSnapshot;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

@Route(value = "users/settings/project", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.projects.page.title")
class ProjectView extends FurmsViewComponent {
	private final ProjectService projectService;
	private final ProjectAllocationService service;
	private final Grid<ProjectAllocationGridModel> grid;
	private String projectId;
	private ProjectAllocationDataSnapshot projectDataSnapshot;
	private BreadCrumbParameter breadCrumbParameter;

	public ProjectView(ProjectService projectService, ProjectAllocationService projectAllocationService) {
		this.projectService = projectService;
		this.service = projectAllocationService;
		this.grid = createCommunityGrid();


		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.project-admin.resource-allocations.page.header")
		);

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
			Optional<ProjectAllocationInstallation> projectAllocationInstallations = projectDataSnapshot.getAllocation(c.id);
			Optional<ProjectDeallocation> deallocation = projectDataSnapshot.getDeallocationStatus(c.id);
			if(deallocation.isPresent()) {
				int statusId = deallocation.get().status.getPersistentId();
				return getStatusLayout(
					getTranslation("view.community-admin.project-allocation.deallocation-status." + statusId),
					deallocation.flatMap(x -> x.errorMessage).map(x -> x.message).orElse(null)
				);
			}
			return projectAllocationInstallations
				.map(installation ->
					getStatusLayout(getTranslation("view.community-admin.project-allocation.status." + installation.status.getPersistentId()),
						installation.errorMessage.map(x -> x.message).orElse(null))
				).orElseGet(HorizontalLayout::new);
		})
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.6"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.7"))
			.setTextAlign(ColumnTextAlign.END);


		grid.setItemDetailsRenderer(new ComponentRenderer<>(c -> ProjectAllocationDetailsComponentFactory
			.create(projectDataSnapshot.getChunks(c.id))));
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
			projectDataSnapshot = new ProjectAllocationDataSnapshot(
				service.findAllInstallations(projectId),
				service.findAllUninstallations(projectId),
				service.findAllChunks(projectId)
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

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String projectId) {
		Project project = handleExceptions(() -> projectService.findById(projectId))
			.flatMap(identity())
			.orElseThrow(IllegalStateException::new);
		this.projectId = projectId;
		breadCrumbParameter = new BreadCrumbParameter(project.getId(), project.getName(), "ALLOCATION");
		loadGridContent();
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}