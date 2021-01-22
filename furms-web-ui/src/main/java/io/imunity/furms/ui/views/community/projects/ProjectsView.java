/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.MenuComponentFactory.createMenuButton;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.RouterLinkFactory.createRouterIcon;
import static io.imunity.furms.ui.utils.RouterLinkFactory.createRouterPool;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Route(value = "community/admin/projects", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.projects.page.title")
public class ProjectsView extends FurmsViewComponent {

	private final ProjectService projectService;
	private final Grid<ProjectViewModel> grid;

	public ProjectsView(ProjectService projectService) {
		this.projectService = projectService;
		this.grid = createCommunityGrid();

		loadGridContent();

		getContent().add(createHeaderLayout(), createSearchFilterLayout(grid), new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout() {
		Button addButton = new Button(getTranslation("view.community-admin.projects.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(ProjectFormView.class));

		HorizontalLayout buttonLayout = new HorizontalLayout(addButton);
		buttonLayout.setWidthFull();
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		buttonLayout.setAlignItems(FlexComponent.Alignment.END);

		H4 headerText = new H4(getTranslation("view.community-admin.projects.header"));
		HorizontalLayout header = new HorizontalLayout(headerText, buttonLayout);
		header.setSizeFull();
		return header;
	}

	private Grid<ProjectViewModel> createCommunityGrid() {
		Grid<ProjectViewModel> grid = new Grid<>(ProjectViewModel.class, false);
		grid.setHeightByRows(true);

		grid.addComponentColumn(c -> new RouterLink(c.name, ProjectsView.class, c.id))
			.setHeader(getTranslation("view.community-admin.projects.grid.column.1"));
		grid.addColumn(c -> c.description)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.2"));
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.3"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createSearchFilterLayout(Grid<ProjectViewModel> grid) {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("view.community-admin.projects.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.addValueChangeListener(event -> {
			String value = event.getValue().toLowerCase();
			List<ProjectViewModel> filteredUsers = loadProjectsViewsModels().stream()
				.filter(project -> project.name.toLowerCase().contains(value))
				.collect(toList());
			grid.setItems(filteredUsers);
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return search;
	}

	private HorizontalLayout createLastColumnContent(ProjectViewModel projectViewModel) {
		HorizontalLayout horizontalLayout = new HorizontalLayout(
			createRouterIcon(USERS, projectViewModel.id, ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM),
			createRouterIcon(PIE_CHART, projectViewModel.id, ProjectView.class, PARAM_NAME, ALLOCATIONS_PARAM),
			createContextMenu(projectViewModel.id, projectViewModel.communityId)
		);
		horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return horizontalLayout;
	}

	private Component createContextMenu(String projectId, String communityId) {
		Icon menu = MENU.create();
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setOpenOnClick(true);
		contextMenu.setTarget(menu);

		contextMenu.addItem(createMenuButton(
			getTranslation("view.community-admin.projects.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ProjectFormView.class, projectId)
		);
		contextMenu.addItem(createMenuButton(
			getTranslation("view.community-admin.projects.menu.delete"), TRASH),
			event -> {
				handleExceptions(() -> projectService.delete(projectId, communityId));
				loadGridContent();
			}
		);

		Component adminComponent = createMenuButton(
			getTranslation("view.community-admin.projects.menu.administrators"),
			USERS
		);
		RouterLink administratorsPool = createRouterPool(adminComponent, projectId, ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM);
		contextMenu.addItem(administratorsPool);

		Component allocationComponent = createMenuButton(
			getTranslation("view.community-admin.projects.menu.allocations"),
			PIE_CHART
		);

		RouterLink allocationsPool = createRouterPool(allocationComponent, projectId, ProjectView.class, PARAM_NAME, ALLOCATIONS_PARAM);
		contextMenu.addItem(allocationsPool);
		getContent().add(contextMenu);
		return menu;
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
