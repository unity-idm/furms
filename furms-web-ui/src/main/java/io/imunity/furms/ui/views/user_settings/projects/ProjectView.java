/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.alarms.AlarmService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.AllocationDetailsComponentFactory;
import io.imunity.furms.ui.components.ResourceProgressBar;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.StatusLayout;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.project_allocation.ProjectAllocationDataSnapshot;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_DOWN;
import static com.vaadin.flow.component.icon.VaadinIcon.ANGLE_RIGHT;
import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
import static com.vaadin.flow.component.icon.VaadinIcon.SPLINE_CHART;
import static io.imunity.furms.domain.resource_access.AccessStatus.GRANTED_STATUES;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Route(value = "users/settings/project", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.projects.page.title")
public class ProjectView extends FurmsViewComponent {
	private final ProjectService projectService;
	private final ProjectAllocationService service;
	private final ResourceAccessService resourceAccessService;
	private final AlarmService alarmService;

	private final Grid<ProjectAllocationGridModel> grid;
	private ProjectId projectId;
	private UsersProjectAllocationDataSnapshot projectDataSnapshot;
	private BreadCrumbParameter breadCrumbParameter;

	public ProjectView(ProjectService projectService, ProjectAllocationService projectAllocationService, ResourceAccessService resourceAccessService, AlarmService alarmService) {
		this.projectService = projectService;
		this.service = projectAllocationService;
		this.alarmService = alarmService;
		this.resourceAccessService = resourceAccessService;
		this.grid = createCommunityGrid();

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.project-admin.resource-allocations.page.header")
		);

		getContent().add(headerLayout, grid);
	}

	private Grid<ProjectAllocationGridModel> createCommunityGrid() {
		Grid<ProjectAllocationGridModel> grid = new DenseGrid<>(ProjectAllocationGridModel.class);

		grid.addComponentColumn(allocation -> {
			Icon icon = grid.isDetailsVisible(allocation) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
			return new Div(icon, new Label(allocation.siteName));
		})
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.1"))
			.setSortable(true);
		grid.addColumn(model -> model.name)
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.2"))
			.setSortable(true)
			.setComparator(model -> model.name.toLowerCase());
		grid.addColumn(model -> model.resourceTypeName)
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.3"))
			.setSortable(true);
		grid.addColumn(model -> model.amountWithUnit)
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.4"))
			.setSortable(true)
			.setComparator(comparing(model -> model.amountWithUnit.amount));
		grid.addColumn(model -> model.consumedWithUnit)
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.5"))
			.setSortable(true)
			.setComparator(comparing(model -> model.consumedWithUnit.amount));
		grid.addColumn(model -> model.remainingWithUnit)
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.6"))
			.setSortable(true)
			.setComparator(comparing(model -> model.remainingWithUnit.amount));
		grid.addComponentColumn(c -> {
			Optional<ProjectAllocationInstallation> projectAllocationInstallations = projectDataSnapshot.getParent().getAllocation(c.id);
			Optional<ProjectDeallocation> deallocation = projectDataSnapshot.getParent().getDeallocationStatus(c.id);
			if(deallocation.isPresent()) {
				int statusId = deallocation.get().status.getPersistentId();
				return new StatusLayout(
					getTranslation("view.community-admin.project-allocation.deallocation-status." + statusId),
					deallocation.flatMap(x -> x.errorMessage).map(x -> x.message).orElse(null)
				);
			}
			return projectAllocationInstallations.map(installation -> {
					final int statusId = installation.status.getPersistentId();
					return new StatusLayout(getTranslation("view.community-admin.project-allocation.status." + statusId),
						installation.errorMessage.map(x -> x.message).orElse(null));
				}).orElseGet(StatusLayout::new);
		})
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.7"))
			.setSortable(true);
		grid.addColumn(x -> getEnabledValue(x.id, x.accessibleForAllProjectMembers))
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.8"))
			.setSortable(true);
		grid.addComponentColumn(model ->
			new ResourceProgressBar(
				model.amountWithUnit.amount,
				model.consumedWithUnit.amount,
				projectDataSnapshot.projectAllocationDataSnapshot.getAlarmThreshold(model.id)
			)
		)
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.9"))
			.setTextAlign(ColumnTextAlign.CENTER);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.user-settings.project-allocation.grid.column.10"))
			.setTextAlign(ColumnTextAlign.END);


		grid.setItemDetailsRenderer(new ComponentRenderer<>(c -> AllocationDetailsComponentFactory
			.create(projectDataSnapshot.getParent().getChunks(c.id), c.amountWithUnit.unit)));
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		return grid;
	}

	private String getEnabledValue(ProjectAllocationId allocationId, boolean accessibleForAllProjectMembers) {
		if(accessibleForAllProjectMembers)
			return getTranslation("view.project-admin.resource-access.grid.access.enabled");
		UserGrant userGrant =  projectDataSnapshot.getUserGrant(allocationId);
		if(userGrant != null && GRANTED_STATUES.contains(userGrant.status))
			return getTranslation("view.project-admin.resource-access.grid.access.enabled");
		return getTranslation("view.project-admin.resource-access.grid.access.disabled");
	}

	private HorizontalLayout createLastColumnContent(ProjectAllocationGridModel projectAllocationGridModel) {
		return new GridActionsButtonLayout(
			new RouterGridLink(SPLINE_CHART, projectAllocationGridModel.id.id.toString(),
				ProjectResourceAllocationsDetailsView.class, "projectId", projectAllocationGridModel.projectId.id.toString()),
			createContextMenu()
		);
	}

	private Component createContextMenu() {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(getTranslation("view.user-settings.ssh-keys.grid.menu.refresh"),
			REFRESH), e -> loadGridContent());

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private void loadGridContent() {
		try {
			projectDataSnapshot = new UsersProjectAllocationDataSnapshot(
				new ProjectAllocationDataSnapshot(
					service.findAllInstallations(projectId),
					service.findAllUninstallations(projectId),
					service.findAllChunks(projectId),
					alarmService.findAll(projectId)
			),
				resourceAccessService.findCurrentUserGrants(projectId).stream()
					.collect(toMap(grant -> grant.projectAllocationId, identity())));
			grid.setItems(loadServicesViewsModels());
		} catch (UserWithoutFenixIdValidationError e){
			showErrorNotification(getTranslation("user.without.fenixid.error.message"));
		}
		catch (Exception e){
			showErrorNotification(getTranslation("base.error.message"));
		}
	}

	private List<ProjectAllocationGridModel> loadServicesViewsModels() {
		return handleExceptions(() -> service.findAllWithRelatedObjects(projectId))
			.orElseGet(Collections::emptySet)
			.stream()
			.map(this::gridMap)
			.sorted(comparing(resourceTypeViewModel -> resourceTypeViewModel.name.toLowerCase()))
			.collect(toList());
	}

	private ProjectAllocationGridModel gridMap(ProjectAllocationResolved projectAllocation) {
		return ProjectAllocationGridModel.builder()
			.id(projectAllocation.id)
			.projectId(projectAllocation.projectId)
			.siteName(projectAllocation.site.getName())
			.resourceTypeName(projectAllocation.resourceType.name)
			.resourceTypeUnit(projectAllocation.resourceType.unit)
			.name(projectAllocation.name)
			.amount(projectAllocation.amount)
			.consumed(projectAllocation.consumed)
			.accessibleForAllProjectMembers(projectAllocation.resourceType.accessibleForAllProjectMembers)
			.build();
	}

	public static class UsersProjectAllocationDataSnapshot {
		private final ProjectAllocationDataSnapshot projectAllocationDataSnapshot;
		private final Map<ProjectAllocationId, UserGrant> allocationIdToGrants;

		UsersProjectAllocationDataSnapshot(ProjectAllocationDataSnapshot projectAllocationDataSnapshot, Map<ProjectAllocationId, UserGrant> allocationIdToGrants) {
			this.projectAllocationDataSnapshot = projectAllocationDataSnapshot;
			this.allocationIdToGrants = allocationIdToGrants;
		}

		ProjectAllocationDataSnapshot getParent() {
			return projectAllocationDataSnapshot;
		}

		UserGrant getUserGrant(ProjectAllocationId allocationId) {
			return allocationIdToGrants.get(allocationId);
		}
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String projectId) {
		ProjectId pId = new ProjectId(projectId);
		Project project = handleExceptions(() -> projectService.findById(pId))
			.flatMap(identity())
			.orElseThrow(IllegalStateException::new);
		this.projectId = pId;
		breadCrumbParameter = new BreadCrumbParameter(
			project.getId().id.toString(), project.getName(), getTranslation("view.user-settings.projects.bread-crumb")
		);
		loadGridContent();
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
