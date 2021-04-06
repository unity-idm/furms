/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.project.ProjectModelResolver;
import io.imunity.furms.ui.project.ProjectViewModel;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.*;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Route(value = "community/admin/projects", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.projects.page.title")
public class ProjectsView extends FurmsViewComponent {

	private final ProjectService projectService;
	private final Grid<ProjectViewModel> grid;
	private final ProjectModelResolver resolver;
	private ZoneId zoneId;

	public ProjectsView(ProjectService projectService, ProjectModelResolver resolver) {
		this.projectService = projectService;
		this.resolver = resolver;
		this.grid = createCommunityGrid();
		UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
			zoneId = ZoneId.of(extendedClientDetails.getTimeZoneId());
		});

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

		grid.addComponentColumn(c -> new RouterGridLink(c.getName(), c.getId(), ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM))
			.setHeader(getTranslation("view.community-admin.projects.grid.column.1"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(c -> c.description)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.2"))
			.setSortable(true);
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
				.filter(project -> project.matches(value))
				.collect(toList());
			grid.setItems(filteredUsers);
			//TODO This is a work around to fix disappearing text cursor
			addButton.focus();
			textField.focus();
		});

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return search;
	}

	private HorizontalLayout createLastColumnContent(ProjectViewModel projectViewModel) {
		return new GridActionsButtonLayout(
			new RouterGridLink(USERS, projectViewModel.id, ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM),
			new RouterGridLink(PIE_CHART, projectViewModel.id, ProjectView.class, PARAM_NAME, ALLOCATIONS_PARAM),
			createContextMenu(projectViewModel.id, projectViewModel.name, projectViewModel.communityId)
		);
	}

	private Component createContextMenu(String projectId, String projectName, String communityId) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(
			getTranslation("view.community-admin.projects.menu.edit"), EDIT),
			event -> UI.getCurrent().navigate(ProjectFormView.class, projectId)
		);

		Dialog confirmDialog = createConfirmDialog(projectId, projectName, communityId);

		contextMenu.addItem(new MenuButton(
			getTranslation("view.community-admin.projects.menu.delete"), TRASH),
			event -> confirmDialog.open()
		);

		MenuButton adminComponent = new MenuButton(
			getTranslation("view.community-admin.projects.menu.administrators"),
			USERS
		);
		RouterLink administratorsPool = new RouterGridLink(adminComponent, projectId, ProjectView.class, PARAM_NAME,
				ADMINISTRATORS_PARAM);
		contextMenu.addItem(administratorsPool);

		MenuButton allocationComponent = new MenuButton(
			getTranslation("view.community-admin.projects.menu.allocations"),
			PIE_CHART
		);

		RouterLink allocationsPool = new RouterGridLink(allocationComponent, projectId, ProjectView.class, PARAM_NAME,
				ALLOCATIONS_PARAM);
		contextMenu.addItem(allocationsPool);
		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private Dialog createConfirmDialog(String projectId, String projectName, String communityId) {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.community-admin.projects.dialog.text", projectName));
		furmsDialog.addConfirmButtonClickListener(event -> {
			handleExceptions(() -> projectService.delete(projectId, communityId));
			loadGridContent();
		});
		return furmsDialog;
	}

	private void loadGridContent() {
		grid.setItems(loadProjectsViewsModels());
	}

	private List<ProjectViewModel> loadProjectsViewsModels() {
		String communityId = getCurrentResourceId();
		List<FURMSUser> users = projectService.findAllUsers(communityId);
		return handleExceptions(() -> projectService.findAll(getCurrentResourceId()))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(p -> resolver.resolve(users, p, zoneId))
			.sorted(comparing(projectViewModel -> projectViewModel.name.toLowerCase()))
			.collect(toList());
	}
}
