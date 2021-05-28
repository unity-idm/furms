/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.projects;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
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
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectDeallocation;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.project_allocation.ProjectAllocationDataSnapshot;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.domain.resource_access.AccessStatus.ENABLED_STATUES;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Route(value = "users/settings/project", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.projects.page.title")
class ProjectView extends FurmsViewComponent {
	private final ProjectService projectService;
	private final ProjectAllocationService service;
	private final ResourceAccessService resourceAccessService;

	private final Optional<FenixUserId> fenixUserId;
	private final Grid<ProjectAllocationGridModel> grid;
	private String projectId;
	private UsersProjectAllocationDataSnapshot projectDataSnapshot;
	private BreadCrumbParameter breadCrumbParameter;

	public ProjectView(ProjectService projectService, ProjectAllocationService projectAllocationService, ResourceAccessService resourceAccessService, AuthzService authzService) {
		this.projectService = projectService;
		this.service = projectAllocationService;
		this.resourceAccessService = resourceAccessService;
		this.grid = createCommunityGrid();
		this.fenixUserId = authzService.getCurrentAuthNUser().fenixUserId;

		ViewHeaderLayout headerLayout = new ViewHeaderLayout(
			getTranslation("view.project-admin.resource-allocations.page.header")
		);

		getContent().add(headerLayout, grid);
	}

	private Grid<ProjectAllocationGridModel> createCommunityGrid() {
		Grid<ProjectAllocationGridModel> grid = new SparseGrid<>(ProjectAllocationGridModel.class);

		grid.addComponentColumn(allocation -> {
			Icon icon = grid.isDetailsVisible(allocation) ? ANGLE_DOWN.create() : ANGLE_RIGHT.create();
			return new Div(icon, new Label(allocation.getSiteName()));
		})
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.1"))
			.setSortable(true);
		grid.addColumn(c -> c.name)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.2"))
			.setSortable(true)
			.setComparator(x -> x.name.toLowerCase());
		grid.addColumn(ProjectAllocationGridModel::getResourceTypeName)
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.3"))
			.setSortable(true);
		grid.addColumn(c -> c.amount.toPlainString() + " " + c.getResourceTypeUnit())
			.setHeader(getTranslation("view.project-admin.resource-allocations.grid.column.5"))
			.setSortable(true)
			.setComparator(comparing(c -> c.amount));
		grid.addComponentColumn(c -> {
			Optional<ProjectAllocationInstallation> projectAllocationInstallations = projectDataSnapshot.getParent().getAllocation(c.id);
			Optional<ProjectDeallocation> deallocation = projectDataSnapshot.getParent().getDeallocationStatus(c.id);
			if(deallocation.isPresent()) {
				int statusId = deallocation.get().status.getPersistentId();
				return getStatusLayout(
					getTranslation("view.community-admin.project-allocation.deallocation-status." + statusId),
					deallocation.flatMap(x -> x.errorMessage).map(x -> x.message).orElse(null)
				);
			}
			return projectAllocationInstallations
				.map(installation ->
					getStatusLayout(getTranslation("view.community-admin.project-allocation.status." + installation.status.getPersistentId()),
						installation.errorMessage.map(x -> x.message).orElse(null))
				).orElseGet(HorizontalLayout::new);
		})
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.6"))
			.setSortable(true);
		grid.addColumn(x -> getEnabledValue(x.id, x.accessibleForAllProjectMembers))
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.5"))
			.setSortable(true);
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.community-admin.project-allocation.grid.column.7"))
			.setTextAlign(ColumnTextAlign.END);


		grid.setItemDetailsRenderer(new ComponentRenderer<>(c -> ProjectAllocationDetailsComponentFactory
			.create(projectDataSnapshot.getParent().getChunks(c.id))));
		grid.setSelectionMode(Grid.SelectionMode.NONE);

		return grid;
	}

	private String getEnabledValue(String allocationId, boolean accessibleForAllProjectMembers) {
		if(accessibleForAllProjectMembers)
			return getTranslation("view.project-admin.resource-access.grid.access.enabled");
		UserGrant userGrant =  projectDataSnapshot.getUserGrant(allocationId);
		if(userGrant != null && ENABLED_STATUES.contains(userGrant.status))
			return getTranslation("view.project-admin.resource-access.grid.access.enabled");
		return getTranslation("view.project-admin.resource-access.grid.access.disabled");
	}

	private HorizontalLayout getStatusLayout(String status, String message) {
		HorizontalLayout horizontalLayout = new HorizontalLayout();
		Text text = new Text(status);
		horizontalLayout.add(text);
		if(message != null){
			Tooltip tooltip = new Tooltip();
			Icon icon = WARNING.create();
			tooltip.attachToComponent(icon);
			tooltip.add(message);
			getContent().add(tooltip);
			horizontalLayout.add(icon);
		}
		return horizontalLayout;
	}

	private HorizontalLayout createLastColumnContent(ProjectAllocationGridModel projectAllocationGridModel) {
		return new GridActionsButtonLayout(
			createContextMenu()
		);
	}

	private Component createContextMenu() {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(getTranslation("view.user-settings.ssh-keys.grid.menu.refresh"),
			REFRESH), e -> loadGridContent(fenixUserId.get()));

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	private void loadGridContent(FenixUserId fenixUserId) {
		handleExceptions(() -> {
			projectDataSnapshot = new UsersProjectAllocationDataSnapshot(
				new ProjectAllocationDataSnapshot(
					service.findAllInstallations(projectId),
					service.findAllUninstallations(projectId),
					service.findAllChunks(projectId)
			),
				resourceAccessService.findUsersGrants(projectId, fenixUserId).stream()
					.collect(toMap(grant -> grant.projectAllocationId, identity())));
			grid.setItems(loadServicesViewsModels());
		});
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
			.resourceTypeUnit(projectAllocation.resourceType.unit.getSuffix())
			.name(projectAllocation.name)
			.amount(projectAllocation.amount)
			.accessibleForAllProjectMembers(projectAllocation.resourceType.accessibleForAllProjectMembers)
			.build();
	}

	public static class UsersProjectAllocationDataSnapshot {
		private final ProjectAllocationDataSnapshot projectAllocationDataSnapshot;
		private final Map<String, UserGrant> allocationIdToGrants;

		UsersProjectAllocationDataSnapshot(ProjectAllocationDataSnapshot projectAllocationDataSnapshot, Map<String, UserGrant> allocationIdToGrants) {
			this.projectAllocationDataSnapshot = projectAllocationDataSnapshot;
			this.allocationIdToGrants = allocationIdToGrants;
		}

		ProjectAllocationDataSnapshot getParent() {
			return projectAllocationDataSnapshot;
		}

		UserGrant getUserGrant(String allocationId) {
			return allocationIdToGrants.get(allocationId);
		}
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String projectId) {
		Project project = handleExceptions(() -> projectService.findById(projectId))
			.flatMap(identity())
			.orElseThrow(IllegalStateException::new);
		this.projectId = projectId;
		breadCrumbParameter = new BreadCrumbParameter(project.getId(), project.getName(), "ALLOCATION");
		if (fenixUserId.isPresent())
			loadGridContent(fenixUserId.get());
		else
			showErrorNotification(getTranslation("user.without.fenixid.error.message"));

	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
