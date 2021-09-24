/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouterLink;

import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.validation.exceptions.RemovalOfConsumedProjectAllocationIsFirbiddenException;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocationStatus;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.ProjectAllocationDetailsComponentFactory;
import io.imunity.furms.ui.components.StatusLayout;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.project_allocation.ProjectAllocationDataSnapshot;

public class ProjectAllocationComponent extends Composite<Div> {

	private final ProjectAllocationService service;

	private final String communityId;
	private final String projectId;
	private final boolean projectExpired;
	private final boolean projectInTerminalState;
	private ProjectAllocationDataSnapshot projectDataSnapshot;

	private final Grid<ProjectAllocationGridModel> grid;
	private final ActionComponent actionComponent;

	public ProjectAllocationComponent(ProjectService projectService, ProjectAllocationService service, String projectId) {
		this.service = service;

		this.communityId = getCurrentResourceId();
		this.projectId = projectId;
		this.projectInTerminalState = projectService.isProjectInTerminalState(communityId, projectId);
		this.projectExpired = projectService.isProjectExpired(projectId);

		this.grid = createCommunityGrid();
		this.actionComponent = new ActionComponent(
				projectId,
				projectService.isProjectExpired(projectId),
				() -> projectService.isProjectInTerminalState(communityId, projectId));

		loadGridContent();
		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.community-admin.project-allocation.page.header"),
			actionComponent
		);

		getContent().add(headerLayout, grid);
	}

	private Grid<ProjectAllocationGridModel> createCommunityGrid() {
		Grid<ProjectAllocationGridModel> grid = new DenseGrid<>(ProjectAllocationGridModel.class);

		grid.addComponentColumn(model -> {
				Icon icon = grid.isDetailsVisible(model) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
				return new Div(icon, new Text(model.siteName));
			})
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.1"))
			.setSortable(true);
		grid.addComponentColumn(model -> {
				if(hasTerminalStatus(model))
					return new RouterLink(model.name, ProjectAllocationFormView.class, model.id);
				return new Text(model.name);
			})
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.2"))
			.setSortable(true)
			.setComparator(model -> model.name.toLowerCase());
		grid.addColumn(model -> model.resourceTypeName)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.4"))
			.setSortable(true);
		grid.addColumn(model -> model.amountWithUnit)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.5"))
			.setSortable(true)
			.setComparator(comparing(c -> c.amountWithUnit.amount));
		grid.addColumn(model -> model.consumedWithUnit)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.6"))
			.setSortable(true)
			.setComparator(comparing(c -> c.consumedWithUnit.amount));
		grid.addColumn(model -> model.remainingWithUnit)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.7"))
			.setSortable(true)
			.setComparator(comparing(c -> c.remainingWithUnit.amount));
		grid.addComponentColumn(c -> {
				Optional<ProjectAllocationInstallation> projectAllocationInstallations = projectDataSnapshot.getAllocation(c.id);
				Optional<ProjectDeallocation> deallocation = projectDataSnapshot.getDeallocationStatus(c.id);
				if(deallocation.isPresent()) {
					int statusId = deallocation.get().status.getPersistentId();
					return new StatusLayout(
						getTranslation("view.community-admin.project-allocation.deallocation-status." + statusId),
						deallocation.flatMap(x -> x.errorMessage).map(x -> x.message).orElse(null),
						getContent()
					);
				}
				return projectAllocationInstallations.map(installation -> {
						final int statusId = installation.status.getPersistentId();
						return new StatusLayout(
								getTranslation("view.community-admin.project-allocation.status." + statusId), 
								installation.errorMessage.map(x -> x.message).orElse(null), 
								getContent());
					})
					.orElseGet(StatusLayout::new);
			})
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.8"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.9"))
			.setTextAlign(ColumnTextAlign.END);

		grid.setItemDetailsRenderer(new ComponentRenderer<>(x -> ProjectAllocationDetailsComponentFactory
			.create(projectDataSnapshot.getChunks(x.id), x.amountWithUnit.unit)));
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		return grid;
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

		if(hasTerminalStatus(model)) {
			contextMenu.addItem(new MenuButton(
					getTranslation("view.community-admin.project-allocation.menu.edit"), EDIT),
				event -> UI.getCurrent().navigate(ProjectAllocationFormView.class, model.id)
			);
		}
		Dialog confirmDialog = createConfirmDialog(model.id, model.name);

		final MenuItem deleteItem = contextMenu.addItem(
				new MenuButton(getTranslation("view.community-admin.project-allocation.menu.delete"), TRASH),
				event -> confirmDialog.open());
		deleteItem.setEnabled(projectInTerminalState && !projectExpired);

		getRefreshMenuItem(contextMenu);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private boolean hasTerminalStatus(ProjectAllocationGridModel model) {
		Optional<ProjectAllocationInstallation> projectAllocationInstallations = projectDataSnapshot.getAllocation(model.id);
		Optional<ProjectDeallocation> deallocation = projectDataSnapshot.getDeallocationStatus(model.id);
		return deallocation.isEmpty() && projectAllocationInstallations.isPresent() && projectAllocationInstallations.get().status.isTerminal();
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
			try {
				service.delete(communityId, projectAllocationId);
				loadGridContent();
			} catch (RemovalOfConsumedProjectAllocationIsFirbiddenException e){
				showErrorNotification(getTranslation("project.allocation.removing.message"));
			} catch (Exception e){
				showErrorNotification(getTranslation("base.error.message"));
			}
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		handleExceptions(() -> {
			projectDataSnapshot = new ProjectAllocationDataSnapshot(
				service.findAllInstallations(projectId),
				service.findAllUninstallations(projectId),
				service.findAllChunks(projectId));
			final List<ProjectAllocationGridModel> items = loadServicesViewsModels();
			items.stream()
					.filter(grid::isDetailsVisible)
					.findFirst()
					.ifPresent(item -> grid.setDetailsVisible(item, false));
			grid.setItems(items);
			grid.getElement().executeJs("this.notifyResize()");
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
		private final Boolean isProjectExpired;
		private final Supplier<Boolean> isProjectInTerminalState;

		ActionComponent(String projectId,
						boolean isProjectExpired,
						Supplier<Boolean> isProjectInTerminalState) {
			this.projectId = projectId;
			this.isProjectExpired = isProjectExpired;
			this.isProjectInTerminalState = isProjectInTerminalState;
			reload();
		}

		void reload() {
			removeAll();

			final Button allocateButton = new Button(getTranslation("view.community-admin.project-allocation.page.button"));
			if (isProjectInTerminalState.get() && !isProjectExpired) {
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
