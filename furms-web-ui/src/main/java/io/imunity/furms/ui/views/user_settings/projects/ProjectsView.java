/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import com.google.common.collect.ImmutableList;
import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.applications.ProjectApplicationsService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.validation.exceptions.UserAlreadyInvitedException;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.utils.CommonExceptionsHandler;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.MINUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.PIE_CHART;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.ACTIVE;
import static io.imunity.furms.ui.views.user_settings.projects.UserStatus.REQUESTED;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "users/settings/projects", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.projects.page.title")
public class ProjectsView extends FurmsViewComponent {
	private final ProjectService projectService;
	private final ProjectApplicationsService projectApplicationsService;
	private final ProjectGridModelMapper mapper;
	private final Grid<ProjectGridModel> grid;
	private final Set<UserStatus> currentFilters = new HashSet<>();
	private String searchText = "";

	ProjectsView(ProjectService projectService, ProjectApplicationsService projectApplicationsService) {
		this.projectService = projectService;
		this.projectApplicationsService = projectApplicationsService;
		this.mapper = new ProjectGridModelMapper(projectService, projectApplicationsService);
		this.grid = createProjectGrid();

		CheckboxGroup<UserStatus> checkboxGroup = createCheckboxLayout();
		loadGridContent();
		getContent().add(createHeaderLayout(checkboxGroup), createSearchFilterLayout(), grid);
	}

	private CheckboxGroup<UserStatus> createCheckboxLayout() {
		CheckboxGroup<UserStatus> checkboxGroup = new CheckboxGroup<>();
		checkboxGroup.setLabel(getTranslation("view.user-settings.projects.filter.title"));
		checkboxGroup.setItems(UserStatus.values());
		checkboxGroup.setItemLabelGenerator(x -> getTranslation(x.filterText));
		checkboxGroup.select(ACTIVE, REQUESTED);
		currentFilters.add(ACTIVE);
		currentFilters.add(REQUESTED);
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

	private HorizontalLayout createSearchFilterLayout() {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("view.user-settings.projects.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setClearButtonVisible(true);
		textField.addValueChangeListener(event -> {
			searchText = textField.getValue().toLowerCase();
			textField.blur();
			loadGridContent();
			textField.focus();
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return search;
	}

	private Grid<ProjectGridModel> createProjectGrid() {
		Grid<ProjectGridModel> grid = new DenseGrid<>(ProjectGridModel.class);

		Grid.Column<ProjectGridModel> firstColumn = grid.addComponentColumn(project -> {
			Component component = new Span(project.name);
			if (project.status.equals(ACTIVE))
				component = new RouterLink(project.name, ProjectView.class, project.id.id.toString());
			return component;
		})
			.setHeader(getTranslation("view.user-settings.projects.grid.column.1"))
			.setSortable(true)
			.setComparator(comparing(project -> project.name))
			.setComparator(project -> project.name.toLowerCase());
		grid.addColumn(project -> project.description)
			.setHeader(getTranslation("view.user-settings.projects.grid.column.2"))
			.setSortable(true);
		grid.addColumn(project -> getTranslation(project.status.gridText))
			.setHeader(getTranslation("view.user-settings.projects.grid.column.3"))
			.setTextAlign(ColumnTextAlign.END)
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.user-settings.projects.grid.column.4"))
			.setTextAlign(ColumnTextAlign.END);
		grid.sort(ImmutableList.of(new GridSortOrder<>(firstColumn, SortDirection.ASCENDING)));
		grid.setAllRowsVisible(false);
		return grid;
	}

	private HorizontalLayout createLastColumnContent(ProjectGridModel project) {
		switch (project.status) {
			case ACTIVE:
				return new GridActionsButtonLayout(
					new RouterGridLink(PIE_CHART, project.id.id.toString(), ProjectView.class),
					createContextMenu(project.id, project.name, project.communityId)
				);
			case NOT_ACTIVE:
				MenuButton applyButton = new MenuButton(PLUS_CIRCLE);
				applyButton.addClickListener(x -> {
					try {
						projectApplicationsService.createForCurrentUser(project.id);
						showSuccessNotification(getTranslation("view.user-settings.projects.applied.notification", project.name));
						loadGridContent();
					} catch (UserAlreadyInvitedException e){
						showErrorNotification(getTranslation("user.already.invited"));
					} catch (Exception e){
						showErrorNotification(getTranslation("base.error.message"));
						throw e;
					}
				});
				return new GridActionsButtonLayout(addApplyTooltip(applyButton));
			case REQUESTED:
				MenuButton removeApplicationButton = new MenuButton(TRASH);
				removeApplicationButton.addClickListener(x -> {
					try {
						projectApplicationsService.removeForCurrentUser(project.id);
					} catch (RuntimeException e){
						CommonExceptionsHandler.showExceptionBasedNotificationError(e);
					}
					loadGridContent();
				});
				return new GridActionsButtonLayout(removeApplicationButton);
			default:
				throw new RuntimeException("This should not happened");
		}
	}

	private MenuButton addApplyTooltip(MenuButton menuButton) {
		Tooltip tooltip = new Tooltip();
		tooltip.add(getTranslation("view.user-settings.projects.apply"));
		tooltip.attachToComponent(menuButton);
		getContent().add(tooltip);
		return menuButton;
	}

	private Component createContextMenu(ProjectId projectId, String projectName, CommunityId communityId) {
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

		RouterLink allocationsPool = new RouterGridLink(allocationComponent, projectId.id.toString(), ProjectView.class);
		contextMenu.addItem(allocationsPool);
		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(ProjectId projectId, String projectName, CommunityId communityId) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.user-settings.projects.dialog.text", projectName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			try {
				projectService.resignFromMembership(communityId, projectId);
				loadGridContent();
			} catch (Exception e){
				showErrorNotification("base.error.message");
			}
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadProjectsViewsModels());
	}

	private List<ProjectGridModel> loadProjectsViewsModels() {
		try {
			Set<Project> projects = handleExceptions(() -> projectService.findAll())
				.orElseGet(Collections::emptySet);
			return mapper.map(projects)
				.stream()
				.sorted(comparing(projectViewModel -> projectViewModel.name.toLowerCase()))
				.filter(project -> currentFilters.contains(project.status))
				.filter(project -> searchText.isEmpty() || project.matches(searchText))
				.collect(toList());
		} catch (UserWithoutFenixIdValidationError e) {
			showErrorNotification(getTranslation("user.without.fenixid.error.message"));
		}
		return List.of();
	}
}
