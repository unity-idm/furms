/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.sites;

import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.resource_access.ResourceAccessService;
import io.imunity.furms.api.users.UserAllocationsService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.domain.resource_access.UserGrant;
import io.imunity.furms.domain.user_operation.UserAddition;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.IconButton;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.StatusLayout;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;
import io.imunity.furms.ui.views.project.resource_access.DenseTreeGrid;
import io.imunity.furms.ui.views.project.resource_access.ResourceAccessModel;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.MINUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
import static io.imunity.furms.domain.resource_access.AccessStatus.PENDING_AND_ACKNOWLEDGED_STATUES;
import static io.imunity.furms.domain.resource_access.AccessStatus.TERMINAL_GRANTED;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static java.util.stream.Collectors.groupingBy;

@Route(value = "project/admin/sites", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.sites.page.title")
public class SitesView extends FurmsViewComponent {
	public final ProjectInstallationsService projectInstallationsService;
	public final UserAllocationsService userAllocationsService;
	public final UserService userService;
	public final TreeGrid<SiteTreeGridModel> grid;
	ResourceAccessService resourceAccessService;


	SitesView(ProjectInstallationsService projectInstallationsService, UserAllocationsService userAllocationsService, UserService userService) {
		this.projectInstallationsService = projectInstallationsService;
		this.userAllocationsService = userAllocationsService;
		this.userService = userService;
		this.grid = new DenseTreeGrid<>();
		fillGrid();
		getContent().add(new ViewHeaderLayout(getTranslation("view.community-admin.projects.header")), grid);
	}

	private Set<SiteTreeGridModel> loadData() {
		String projectId = getCurrentResourceId();
		Map<String, ProjectUpdateJobStatus> collect = projectInstallationsService.findAllUpdatesByProjectId(projectId).stream()
			.collect(Collectors.toMap(x -> x.siteId, x -> x));

		return projectInstallationsService.findAllByProjectId(projectId).stream()
			.map(statusJob -> {
				String status = Optional.ofNullable(collect.get(statusJob.siteId))
					.map(x -> getTranslation("project.update.status." + x.status.getPersistentId()))
					.orElse(getTranslation("project.installation.status." + statusJob.status.getPersistentId()));
				String message = statusJob.errorMessage
					.map(y -> y.message)
					.orElse(null);
				return SiteTreeGridModel.builder()
					.siteId(statusJob.siteId)
					.siteName(statusJob.siteName)
					.status(status)
					.message(message)
					.build();
			})
			.collect(Collectors.toSet());
	}

	private Set<SiteTreeGridModel> loadNextLevelData(String siteId) {

		Set<UserAddition> allByProjectId = userAllocationsService.findAllByProjectId(getCurrentResourceId());
		Map<FenixUserId, FURMSUser> users = userService.getAllUsers().stream()
			.filter(x -> x.fenixUserId.isPresent())
			.collect(Collectors.toMap(usr -> usr.fenixUserId.get(), Function.identity()));

		return  allByProjectId.stream()
			.filter(x -> x.siteId.id.equals(siteId))
			.map(x -> {
				FenixUserId userId = new FenixUserId(x.userId);
				FURMSUser furmsUser = users.get(userId);
				return SiteTreeGridModel.builder()
					.siteId(x.siteId.id)
					.userId(userId)
					.userEmail(furmsUser.email)
					.userStatus(furmsUser.status.name())
					.userAccessStatus(x.status)
					.build();
			})
			.collect(Collectors.toSet());
	}

	private void fillGrid() {
		grid.addHierarchyColumn(model -> model.siteName)
			.setHeader(getTranslation("view.project-admin.sites.grid.1"))
			.setSortable(true)
			.setFlexGrow(25);
		grid.addComponentColumn(model -> new StatusLayout(model.status, model.message, getContent()))
			.setHeader(getTranslation("view.project-admin.sites.grid.2"))
			.setSortable(true)
			.setFlexGrow(6);
		grid.addColumn(model -> model.userName)
			.setHeader(getTranslation("view.project-admin.sites.grid.3"))
			.setSortable(true)
			.setFlexGrow(25);
		grid.addColumn(model -> model.userEmail)
			.setHeader(getTranslation("view.project-admin.sites.grid.4"))
			.setSortable(true)
			.setFlexGrow(25);
		grid.addColumn(model -> model.userStatus)
			.setHeader(getTranslation("view.project-admin.sites.grid.5"))
			.setSortable(true)
			.setFlexGrow(25);
		grid.addColumn(model -> model.userAccessStatus)
			.setHeader(getTranslation("view.project-admin.sites.grid.6"))
			.setSortable(true)
			.setFlexGrow(25);
		grid.addComponentColumn(resourceAccessModel -> {
			if(resourceAccessModel.siteName != null || isInstalling()) {
				IconButton iconButton = new IconButton(REFRESH.create());
				iconButton.addClickListener(event -> grid.setItems(loadData(), key -> loadNextLevelData(key.siteId)));
				return iconButton;
			}
			else {
				GridActionMenu contextMenu = new GridActionMenu();
				if(isInstalled()) {
					contextMenu.addItem(new MenuButton(
							getTranslation("view.community-admin.projects.menu.refresh"), MINUS_CIRCLE),
						event -> loadGridContent()
					);
				}
				else {
					contextMenu.addItem(new MenuButton(
							getTranslation("view.community-admin.projects.menu.refresh"), PLUS_CIRCLE),
						event -> loadGridContent()
					);
				}
				return new GridActionsButtonLayout(contextMenu.getTarget());
			}

		})
			.setHeader(getTranslation("view.project-admin.sites.grid.7"));


		grid.setItems(loadData(), key -> loadNextLevelData(key.siteId));
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
	}

	private Map<FenixUserId, List<UserGrant>> loadUsersGrants() {
		return resourceAccessService.findUsersGrants(getCurrentResourceId()).stream()
			.collect(groupingBy(x -> new FenixUserId(x.userId)));
	}

	public boolean isGrantOrRevokeAvailable(FenixUserId userId) {
		return loadUsersGrants().getOrDefault(userId, List.of()).stream()
			.filter(x -> PENDING_AND_ACKNOWLEDGED_STATUES.contains(x.status))
			.isEmpty();
	}
}
