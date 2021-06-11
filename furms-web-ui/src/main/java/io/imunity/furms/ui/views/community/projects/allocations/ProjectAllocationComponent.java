/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import com.vaadin.componentfactory.Tooltip;
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
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.project_allocation.ProjectAllocationDataSnapshot;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;


public class ProjectAllocationComponent extends Composite<Div> {

	private final Grid<ProjectAllocationGridModel> grid;
	private final ProjectAllocationService service;
	private final String communityId;
	private final String projectId;
	private final ActionComponent actionComponent;
	private ProjectAllocationDataSnapshot projectDataSnapshot;

	public ProjectAllocationComponent(ProjectService projectService, ProjectAllocationService service, String projectId) {
		this.communityId = getCurrentResourceId();
		this.service = service;
		this.projectId = projectId;
		this.grid = createCommunityGrid();
		this.actionComponent = new ActionComponent(
				projectId,
				() -> projectService.isProjectExpired(projectId),
				() -> projectService.isProjectInTerminalState(communityId, projectId));

		loadGridContent();
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.community-admin.project-allocation.page.header"),
			actionComponent
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
		grid.addColumn(ProjectAllocationGridModel::getAmountWithUnit)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.5"))
			.setSortable(true)
			.setComparator(comparing(c -> c.getAmountWithUnit().amount));
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
				.map(installation -> getStatusLayout(getTranslation("view.community-admin.project-allocation.status." + installation.status.getPersistentId()), installation.errorMessage.map(x -> x.message).orElse(null)))
				.orElseGet(HorizontalLayout::new);
		})
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.6"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.7"))
			.setTextAlign(ColumnTextAlign.END);

		grid.setItemDetailsRenderer(new ComponentRenderer<>(x -> ProjectAllocationDetailsComponentFactory
			.create(projectDataSnapshot.getChunks(x.id))));
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		return grid;
	}

	private HorizontalLayout getStatusLayout(String status, String message) {
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

	private Component createLastColumnContent(ProjectAllocationGridModel model) {
		Optional<ProjectDeallocation> deallocation = projectDataSnapshot.getDeallocationStatus(model.id);
		if(deallocation.isPresent() && !ProjectDeallocationStatus.FAILED.equals(deallocation.get().status)){
			return getRefreshMenuItem(new GridActionMenu());
		}
		return new GridActionsButtonLayout(
			createContextMenu(model)
		);
	}

	private Component createContextMenu(ProjectAllocationGridModel model) {
		GridActionMenu contextMenu = new GridActionMenu();

		Dialog confirmDialog = createConfirmDialog(model.id, model.name);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.community-admin.project-allocation.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		getRefreshMenuItem(contextMenu);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Component getRefreshMenuItem(GridActionMenu contextMenu) {
		contextMenu.addItem(new MenuButton(getTranslation("view.user-settings.ssh-keys.grid.menu.refresh"),
			REFRESH), e -> {
			loadGridContent();
		});
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
			projectDataSnapshot = new ProjectAllocationDataSnapshot(
				service.findAllInstallations(projectId),
				service.findAllUninstallations(projectId),
				service.findAllChunks(projectId));
			grid.setItems(loadServicesViewsModels());
			actionComponent.reload();
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

	private static class ActionComponent extends Div {

		private final String projectId;
		private final Supplier<Boolean> isProjectExpired;
		private final Supplier<Boolean> isProjectInTerminalState;

		ActionComponent(String projectId,
						Supplier<Boolean> isProjectExpired,
						Supplier<Boolean> isProjectInTerminalState) {
			this.projectId = projectId;
			this.isProjectExpired = isProjectExpired;
			this.isProjectInTerminalState = isProjectInTerminalState;
			reload();
		}

		void reload() {
			removeAll();

			final Button allocateButton = new Button(getTranslation("view.community-admin.project-allocation.page.button"));
			if (isProjectInTerminalState.get() && !isProjectExpired.get()) {
				allocateButton.addClickListener(x -> UI.getCurrent().navigate(
						new RouterLink("", ProjectAllocationFormView.class).getHref(),
						QueryParameters.simple(Map.of("projectId", projectId))));
			} else {
				allocateButton.setEnabled(false);
			}
			add(allocateButton);
		}
	}
}
