/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PIE_CHART;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.icon.VaadinIcon.USERS;
import static io.imunity.furms.ui.utils.MenuComponentFactory.createActionButton;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.RouterLinkFactory.createRouterIcon;
import static io.imunity.furms.ui.utils.RouterLinkFactory.createRouterPool;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.ADMINISTRATORS_PARAM;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.ALLOCATIONS_PARAM;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.PARAM_NAME;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

@Route(value = "community/admin/projects", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.projects.page.title")
public class ProjectsView extends FurmsViewComponent {

	private final ProjectService projectService;
	private final Grid<ProjectViewModel> grid;

	public ProjectsView(ProjectService projectService) {
		this.projectService = projectService;
		this.grid = createCommunityGrid();

		Button addButton = createAddButton();
		loadGridContent();

		getContent().add(createHeaderLayout(addButton), createSearchFilterLayout(grid, addButton), new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout(Button addButton) {
		return new ViewHeaderLayout(getTranslation("view.community-admin.projects.header"), addButton);
	}

	private Button createAddButton() {
		Button addButton = new Button(getTranslation("view.community-admin.projects.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(ProjectFormView.class));
		return addButton;
	}

	private Grid<ProjectViewModel> createCommunityGrid() {
		Grid<ProjectViewModel> grid = new SparseGrid<>(ProjectViewModel.class);

		grid.addComponentColumn(c -> new RouterLink(c.name, ProjectView.class, c.id))
			.setHeader(getTranslation("view.community-admin.projects.grid.column.1"));
		grid.addColumn(c -> c.description)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.2"));
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.3"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createSearchFilterLayout(Grid<ProjectViewModel> grid, Button addButton) {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("view.community-admin.projects.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setClearButtonVisible(true);
		textField.addValueChangeListener(event -> {
			String value = textField.getValue().toLowerCase();
			List<ProjectViewModel> filteredUsers = loadProjectsViewsModels().stream()
				.filter(project -> project.name.toLowerCase().contains(value))
				.collect(toList());
			grid.setItems(filteredUsers);
			//This is a work around to fix disappearing text cursor
			addButton.focus();
			textField.focus();
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return search;
	}

	private HorizontalLayout createLastColumnContent(ProjectViewModel projectViewModel) {
		return new GridActionsButtonLayout(
			createRouterIcon(USERS, projectViewModel.id, ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM),
			createRouterIcon(PIE_CHART, projectViewModel.id, ProjectView.class, PARAM_NAME, ALLOCATIONS_PARAM),
			createContextMenu(projectViewModel.id, projectViewModel.communityId)
		);
	}

	private Component createContextMenu(String projectId, String communityId) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(createActionButton(
			getTranslation("view.community-admin.projects.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ProjectFormView.class, projectId)
		);
		contextMenu.addItem(createActionButton(
			getTranslation("view.community-admin.projects.menu.delete"), TRASH),
			event -> {
				handleExceptions(() -> projectService.delete(projectId, communityId));
				loadGridContent();
			}
		);

		Component adminComponent = createActionButton(
			getTranslation("view.community-admin.projects.menu.administrators"),
			USERS
		);
		RouterLink administratorsPool = createRouterPool(adminComponent, projectId, ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM);
		contextMenu.addItem(administratorsPool);

		Component allocationComponent = createActionButton(
			getTranslation("view.community-admin.projects.menu.allocations"),
			PIE_CHART
		);

		RouterLink allocationsPool = createRouterPool(allocationComponent, projectId, ProjectView.class, PARAM_NAME, ALLOCATIONS_PARAM);
		contextMenu.addItem(allocationsPool);
		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private void loadGridContent() {
		grid.setItems(loadProjectsViewsModels());
	}

	private Set<ProjectViewModel> loadProjectsViewsModels() {
		return handleExceptions(() -> projectService.findAll(getCurrentResourceId()))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(ProjectViewModelMapper::map)
			.collect(toSet());
	}
}
