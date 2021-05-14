/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.ui.components.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

public class ProjectAllocationComponent extends Composite<Div> {

	private final Grid<ProjectAllocationGridModel> grid;
	private final ProjectAllocationService service;
	private final String communityId;
	private final String projectId;
	private GroupedProjectAllocationsSnapshot groupedProjectAllocations;

	public ProjectAllocationComponent(ProjectAllocationService service, String projectId) {
		this.communityId = getCurrentResourceId();
		this.service = service;
		this.projectId = projectId;
		this.grid = createCommunityGrid();
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

		grid.addComponentColumn(allocation -> {
			Icon icon = grid.isDetailsVisible(allocation) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
			return new Div(icon, new Text(allocation.getSiteName()));
		})
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.1"))
			.setSortable(true);
		grid.addColumn(c -> c.name)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.2"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(ProjectAllocationGridModel::getResourceTypeName)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.4"))
			.setSortable(true);
		grid.addColumn(c -> c.amount.toPlainString() + " " + c.getResourceTypeUnit())
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.5"))
			.setSortable(true)
			.setComparator(comparing(c -> c.amount));
		grid.addComponentColumn(c -> {
			List<ProjectAllocationInstallation> projectAllocationInstallations = groupedProjectAllocations.getAllocation(c.id);
			ProjectDeallocation deallocation = groupedProjectAllocations.getDeallocation(c.id);
			if(deallocation != null && deallocation.status.equals(ProjectDeallocationStatus.FAILED)) {
				return getFailedLayout(getTranslation("view.community-admin.project-allocation.status.6"), deallocation.message);
			}
			return projectAllocationInstallations.stream()
				.max(comparing(projectAllocationInstallationStatus -> projectAllocationInstallationStatus.status.getPersistentId()))
				.map(installation -> getFailedLayout(getTranslation("view.community-admin.project-allocation.status." + installation.status.getPersistentId()), installation.message))
				.orElseGet(HorizontalLayout::new);
		})
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.6"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.7"))
			.setTextAlign(ColumnTextAlign.END);

		grid.setItemDetailsRenderer(new ComponentRenderer<>(x -> ProjectAllocationDetailsComponentFactory
			.create(groupedProjectAllocations.getAllocation(x.id))));
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		return grid;
	}

	private HorizontalLayout getFailedLayout(String status, String message) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		Text text = new Text(status);
		horizontalLayout.add(text);
		if(message != null) {
			Tooltip tooltip = new Tooltip();
			Icon icon = WARNING.create();
			tooltip.attachToComponent(icon);
			tooltip.add(message);
			getContent().add(tooltip);
			horizontalLayout.add(icon);
		}
		return horizontalLayout;
	}

	private Component createLastColumnContent(ProjectAllocationGridModel projectAllocationGridModel) {
		if(groupedProjectAllocations.getDeallocation(projectAllocationGridModel.id).status.equals(ProjectDeallocationStatus.FAILED)){
			return new Div();
		}
		return new GridActionsButtonLayout(
			createContextMenu(projectAllocationGridModel)
		);
	}

	private Component createContextMenu(ProjectAllocationGridModel model) {
		GridActionMenu contextMenu = new GridActionMenu();

		Dialog confirmDialog = createConfirmDialog(model.id, model.name);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.community-admin.project-allocation.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		contextMenu.addItem(new MenuButton(getTranslation("view.user-settings.ssh-keys.grid.menu.refresh"),
			REFRESH), e -> {
			loadGridContent();
		});

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
		handleExceptions(() -> {
			groupedProjectAllocations = new GroupedProjectAllocationsSnapshot(
				service.findAllInstallations(communityId, projectId),
				service.findAllUninstallations(communityId, projectId));
			grid.setItems(loadServicesViewsModels());
		});
	}

	private List<ProjectAllocationGridModel> loadServicesViewsModels() {
		return handleExceptions(() -> service.findAllWithRelatedObjects(communityId, projectId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(ProjectAllocationModelsMapper::gridMap)
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.name.toLowerCase()))
			.collect(toList());
	}
	
	private static class GroupedProjectAllocationsSnapshot {
		private final Map<String, List<ProjectAllocationInstallation>> groupedProjectAllocations;
		private final Map<String, ProjectDeallocation> groupedProjectDeallocations;

		GroupedProjectAllocationsSnapshot(Set<ProjectAllocationInstallation> installations, Set<ProjectDeallocation> uninstallations) {
			this.groupedProjectAllocations = installations.stream()
					.collect(groupingBy(installation -> installation.projectAllocationId));
			this.groupedProjectDeallocations = uninstallations.stream()
				.collect(toMap(uninstallation -> uninstallation.projectAllocationId, identity()));
		}
		
		List<ProjectAllocationInstallation> getAllocation(String projectAllocationId) {
			return groupedProjectAllocations.getOrDefault(projectAllocationId, emptyList());
		}

		ProjectDeallocation getDeallocation(String projectAllocationId) {
			return groupedProjectDeallocations.get(projectAllocationId);
		}
	}
}
