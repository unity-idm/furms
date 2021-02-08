/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.ACTIVE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "users/settings/projects", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.projects.page.title")
public class ProjectsView extends FurmsViewComponent {
	private final ProjectService projectService;
	private final ProjectGridModelMapper mapper;
	private final Grid<ProjectGridModel> grid;
	private final String currentUserId;
	private final Set<UserStatus> currentFilters = new HashSet<>();

	ProjectsView(ProjectService projectService, AuthzService authzService) {
		this.currentUserId = authzService.getCurrentUserId();
		this.projectService = projectService;
		this.mapper = new ProjectGridModelMapper(currentUserId, projectService);
		this.grid = createProjectGrid();
		loadGridContent();

		CheckboxGroup<UserStatus> checkboxGroup = createCheckboxLayout();
		getContent().add(createHeaderLayout(checkboxGroup), createSearchFilterLayout(grid), new HorizontalLayout(grid));
	}

	private CheckboxGroup<UserStatus> createCheckboxLayout() {
		CheckboxGroup<UserStatus> checkboxGroup = new CheckboxGroup<>();
		checkboxGroup.setLabel(getTranslation("view.user-settings.projects.filter.title"));
		checkboxGroup.setItems(UserStatus.values());
		checkboxGroup.setItemLabelGenerator(x -> getTranslation(x.filterText));
		checkboxGroup.addSelectionListener(event -> {
			currentFilters.clear();
			currentFilters.addAll(event.getAllSelectedItems());
			loadGridContent();
		});

		checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
		return checkboxGroup;
	}

	private HorizontalLayout createHeaderLayout(CheckboxGroup<UserStatus> checkboxGroup) {
		return new ViewHeaderLayout(getTranslation("view.user-settings.projects.header"), checkboxGroup);
	}

	private HorizontalLayout createSearchFilterLayout(Grid<ProjectGridModel> grid) {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("view.user-settings.projects.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setClearButtonVisible(true);
		textField.addValueChangeListener(event -> {
			String value = textField.getValue().toLowerCase();
			List<ProjectGridModel> filteredUsers = loadProjectsViewsModels().stream()
				.filter(project -> project.matches(value))
				.collect(toList());
			textField.blur();
			grid.setItems(filteredUsers);
			textField.focus();
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return search;
	}

	private Grid<ProjectGridModel> createProjectGrid() {
		Grid<ProjectGridModel> grid = new SparseGrid<>(ProjectGridModel.class);

		grid.addComponentColumn(project -> {
			Component component = new Span(project.name);
			if(project.status.equals(ACTIVE))
				component = new RouterLink(project.name, ProjectView.class, project.id);
			return component;
		})
			.setHeader(getTranslation("view.user-settings.projects.grid.column.1"))
			.setSortable(true)
			.setComparator(project -> project.name.toLowerCase());
		grid.addColumn(project -> project.description)
			.setHeader(getTranslation("view.user-settings.projects.grid.column.2"))
			.setSortable(true);
		grid.addColumn(project -> getTranslation(project.status.gridText))
			.setHeader(getTranslation("view.user-settings.projects.grid.column.3"))
			.setTextAlign(ColumnTextAlign.END);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.user-settings.projects.grid.column.4"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ProjectGridModel project) {
		switch (project.status) {
			case ACTIVE:
				return new GridActionsButtonLayout(
					new RouterGridLink(PIE_CHART, project.id, ProjectView.class),
					createContextMenu(project.id, project.name, project.communityId)
				);
			case NOT_ACTIVE:
				return new GridActionsButtonLayout(new MenuButton(PLUS_CIRCLE));
			case REQUESTED:
				return new GridActionsButtonLayout(new MenuButton(TRASH));
			default:
				throw new RuntimeException();
		}
	}

	private Component createContextMenu(String projectId, String projectName, String communityId) {
		GridActionMenu contextMenu = new GridActionMenu();
		Dialog confirmDialog = createConfirmDialog(projectId, projectName, communityId);

		contextMenu.addItem(new MenuButton(
				getTranslation("view.user-settings.projects.context.menu.leave"), MINUS_CIRCLE),
			event -> confirmDialog.open()
		);

		MenuButton allocationComponent = new MenuButton(
			getTranslation("view.user-settings.projects.menu.allocations"),
			PIE_CHART
		);

		RouterLink allocationsPool = new RouterGridLink(allocationComponent, projectId, ProjectView.class);
		contextMenu.addItem(allocationsPool);
		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String projectId, String projectName, String communityId) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.user-settings.projects.dialog.text", projectName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> projectService.removeMember(communityId, projectId, currentUserId));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadProjectsViewsModels()
			.stream()
			.filter(project -> currentFilters.isEmpty() || currentFilters.contains(project.status))
		);
	}

	private List<ProjectGridModel> loadProjectsViewsModels() {
		return handleExceptions(() -> projectService.findAll())
			.orElseGet(Collections::emptySet)
			.stream()
			.map(mapper::map)
			.sorted(comparing(projectViewModel -> projectViewModel.name.toLowerCase()))
			.collect(toList());
	}
}
