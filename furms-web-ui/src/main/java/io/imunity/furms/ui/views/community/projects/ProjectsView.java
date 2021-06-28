/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.componentfactory.TooltipAlignment;
import com.vaadin.componentfactory.TooltipPosition;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
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
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.StatusLayout;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PIE_CHART;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
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
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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
		this.projectsViewDataSnapshot = new ProjectsViewDataSnapshot();
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

		grid.addComponentHierarchyColumn(this::createNameComponent)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.1"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(c -> c.description)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.2"))
			.setSortable(true);
		grid.addColumn(c -> c.siteName)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.3"))
			.setSortable(true);
		grid.addComponentColumn(c -> new StatusLayout(c.status, c.message))
			.setHeader(getTranslation("view.community-admin.projects.grid.column.4"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.projects.grid.column.5"))
			.setTextAlign(ColumnTextAlign.END);

		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

		return grid;
	}

	private HorizontalLayout createNameComponent(ProjectViewModel projectViewModel) {
		final HorizontalLayout nameComponent = new HorizontalLayout();

		if (projectViewModel.isExpired()) {
			final Icon warningIcon = TIME_BACKWARD.create();
			final Tooltip warningTooltip = new Tooltip(warningIcon, TooltipPosition.BOTTOM, TooltipAlignment.CENTER);
			warningTooltip.add(getTranslation("view.community-admin.projects.grid.column.1.expired.project.tooltip"));
			getContent().add(warningTooltip);

			nameComponent.add(warningIcon);
		}

		nameComponent.add(new RouterGridLink(projectViewModel.getName(), projectViewModel.getId(),
				ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM));

		return nameComponent;
	}

	private HorizontalLayout createSearchFilterLayout(Grid<ProjectViewModel> grid, Button addButton) {
	private HorizontalLayout createSearchFilterLayout(Grid<ProjectViewGridModel> grid, Button addButton) {
		TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("view.community-admin.projects.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.setClearButtonVisible(true);
		textField.addValueChangeListener(event -> {
			String value = textField.getValue().toLowerCase();
			List<ProjectViewGridModel> filteredUsers = projectsViewDataSnapshot.projectViewGridModels.stream()
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
		if(projectViewModel.communityId == null){
			GridActionMenu contextMenu = new GridActionMenu();
			contextMenu.addItem(new MenuButton(
					getTranslation("view.community-admin.projects.menu.refresh"), REFRESH),
				event -> loadGridContent()
			);
			return new GridActionsButtonLayout(contextMenu.getTarget());
		}
		return new GridActionsButtonLayout(
			new RouterGridLink(USERS, projectViewModel.projectId, ProjectView.class, PARAM_NAME, ADMINISTRATORS_PARAM),
			new RouterGridLink(PIE_CHART, projectViewModel.projectId, ProjectView.class, PARAM_NAME, ALLOCATIONS_PARAM),
			createContextMenu(projectViewModel.projectId, projectViewModel.name, projectViewModel.communityId)
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
		Set<ProjectViewGridModel> currentExpandedItems = projectsViewDataSnapshot.projectViewGridModels.stream()
			.filter(grid::isExpanded)
			.collect(Collectors.toSet());
		projectsViewDataSnapshot = new ProjectsViewDataSnapshot();
		grid.setItems(projectsViewDataSnapshot.projectViewGridModels, key -> projectsViewDataSnapshot.getProjectStatuses(key.projectId));
		grid.expand(currentExpandedItems);
	}

	private class ProjectsViewDataSnapshot {
		public final List<ProjectViewGridModel> projectViewGridModels;
		private final Map<String, List<ProjectViewGridModel>> projectInstallationJobStatusByProjectId;

		ProjectsViewDataSnapshot() {
			String communityId = getCurrentResourceId();

			this.projectViewGridModels = handleExceptions(() -> projectService.findAllByCommunityId(communityId))
				.orElseGet(Collections::emptySet)
				.stream()
				.map(p -> ProjectViewGridModel.builder()
					.id(p.getId())
					.projectId(p.getId())
					.communityId(p.getCommunityId())
					.name(p.getName())
					.description(p.getDescription())
					.build())
				.sorted(comparing(projectViewModel -> projectViewModel.name.toLowerCase()))
				.collect(toList());

			Map<String, Map<String, ProjectUpdateJobStatus>> collect = projectInstallationStatusService.findAllUpdatesByCommunityId(communityId).stream()
				.collect(
					groupingBy(
						job -> job.projectId,
						toMap(job -> job.siteId, Function.identity())
					)
				);

			this.projectInstallationJobStatusByProjectId = projectInstallationStatusService.findAllByCommunityId(communityId).stream()
				.collect(groupingBy(
					jobStatus -> jobStatus.projectId,
					collectingAndThen(
						mapping(
							jobStatus -> {
								String status = getString(collect, jobStatus);
								String message = jobStatus.errorMessage.map(y -> y.message).orElse(null);
								return mapGrid(jobStatus, status, message);
								},
							toList()
						),
						jobStatuses -> {
							jobStatuses.sort(Comparator.comparing(jobStatus -> jobStatus.siteName));
							return jobStatuses;
						})
				));
		}

		private ProjectViewGridModel mapGrid(ProjectInstallationJobStatus jobStatus, String status, String message) {
			return ProjectViewGridModel.builder()
				.id(jobStatus.projectId + jobStatus.siteId)
				.siteName(jobStatus.siteName)
				.status(status)
				.message(message)
				.build();
		}

		private String getString(Map<String, Map<String, ProjectUpdateJobStatus>> collect, ProjectInstallationJobStatus x) {
			return Optional.ofNullable(collect.get(x.projectId))
				.flatMap(y -> Optional.ofNullable(y.get(x.siteId)))
				.map(y -> getTranslation("project.update.status." + y.status.getPersistentId()))
				.orElse(getTranslation("project.installation.status." + x.status.getPersistentId()));
		}

		public List<ProjectViewGridModel> getProjectStatuses(String projectId) {
			return projectInstallationJobStatusByProjectId.getOrDefault(projectId, emptyList());
		}
	}
}
