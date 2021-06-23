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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.project_installation.ProjectInstallationStatusService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.project_installation.ProjectInstallationJobStatus;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.project.ProjectViewGridModel;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PIE_CHART;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.icon.VaadinIcon.USERS;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.ADMINISTRATORS_PARAM;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.ALLOCATIONS_PARAM;
import static io.imunity.furms.ui.views.community.projects.ProjectConst.PARAM_NAME;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Route(value = "community/admin/projects", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.projects.page.title")
public class ProjectsView extends FurmsViewComponent {

	private final ProjectService projectService;
	private final ProjectInstallationStatusService projectInstallationStatusService;
	private final TreeGrid<ProjectViewGridModel> grid;
	private ProjectsViewDataSnapshot projectsViewDataSnapshot;

	public ProjectsView(ProjectService projectService, ProjectInstallationStatusService projectInstallationStatusService) {
		this.projectService = projectService;
		this.projectInstallationStatusService = projectInstallationStatusService;
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

	private TreeGrid<ProjectViewGridModel> createCommunityGrid() {
		TreeGrid<ProjectViewGridModel> grid = new TreeGrid<>();

		grid.addComponentColumn(c -> new RouterGridLink(c.name, c.id, ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM))
			.setHeader(getTranslation("view.community-admin.projects.grid.column.1"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(c -> c.description)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.2"))
			.setSortable(true);
		grid.addColumn(c -> c.siteName)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.2"))
			.setSortable(true);
		grid.addColumn(c -> c.status)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.2"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.3"))
			.setTextAlign(ColumnTextAlign.END);

		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

		return grid;
	}

	private HorizontalLayout createSearchFilterLayout(Grid<ProjectViewGridModel> grid, Button addButton) {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("view.community-admin.projects.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setClearButtonVisible(true);
		textField.addValueChangeListener(event -> {
			String value = textField.getValue().toLowerCase();
			List<ProjectViewGridModel> filteredUsers = loadProjectsViewsModels().stream()
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

	private HorizontalLayout createLastColumnContent(ProjectViewGridModel projectViewModel) {
		return new GridActionsButtonLayout(
			new RouterGridLink(USERS, projectViewModel.id, ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM),
			new RouterGridLink(PIE_CHART, projectViewModel.id, ProjectView.class, PARAM_NAME, ALLOCATIONS_PARAM),
			createContextMenu(projectViewModel.id, projectViewModel.name, projectViewModel.communityId)
		);
	}

	private Component createContextMenu(String projectId, String projectName, String communityId) {
		GridActionMenu contextMenu = new GridActionMenu();

		if(projectService.isProjectInTerminalState(communityId, projectId)){
			contextMenu.addItem(new MenuButton(
					getTranslation("view.community-admin.projects.menu.edit"), EDIT),
				event -> UI.getCurrent().navigate(ProjectFormView.class, projectId)
			);

			Dialog confirmDialog = createConfirmDialog(projectId, projectName, communityId);

			contextMenu.addItem(new MenuButton(
					getTranslation("view.community-admin.projects.menu.delete"), TRASH),
				event -> confirmDialog.open()
			);
		}

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
		grid.setItems(loadProjectsViewsModels(), key -> projectsViewDataSnapshot.getProjectStatuses(key.id));
	}

	private List<ProjectViewGridModel> loadProjectsViewsModels() {
		String communityId = getCurrentResourceId();
		projectsViewDataSnapshot = new ProjectsViewDataSnapshot(projectInstallationStatusService.findAllByCommunityId(communityId));
		return handleExceptions(() -> projectService.findAllByCommunityId(communityId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(p -> ProjectViewGridModel.builder()
				.id(p.getId())
				.communityId(p.getCommunityId())
				.name(p.getName())
				.description(p.getDescription())
				.build())
			.sorted(comparing(projectViewModel -> projectViewModel.name.toLowerCase()))
			.collect(toList());
	}

	private static class ProjectsViewDataSnapshot {
		private final Map<String, List<ProjectViewGridModel>> projectInstallationJobStatusByProjectId;

		ProjectsViewDataSnapshot(Set<ProjectInstallationJobStatus> statuses) {
			this.projectInstallationJobStatusByProjectId = statuses.stream()
				.map(x -> ProjectViewGridModel.builder()
					.id(x.projectId)
					.siteName(x.siteName)
					.status(x.status)
					.build())
				.collect(groupingBy(jobStatus -> jobStatus.id, collectingAndThen(toList(), jobStatuses -> {
					jobStatuses.sort(Comparator.comparing(jobStatus -> jobStatus.siteName));
						return jobStatuses;
					})
				));
		}

		public List<ProjectViewGridModel> getProjectStatuses(String projectId) {
			return projectInstallationJobStatusByProjectId.getOrDefault(projectId, emptyList());
		}
	}
}
