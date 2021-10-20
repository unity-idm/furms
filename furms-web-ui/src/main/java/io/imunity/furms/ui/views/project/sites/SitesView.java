/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.sites;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.api.user_site_access.UserSiteAccessService;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.domain.user_site_access.UsersSitesAccesses;
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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.MINUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

@Route(value = "project/admin/sites", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.sites.page.title")
public class SitesView extends FurmsViewComponent {
	public final UserSiteAccessService userSiteAccessService;
	public final ProjectInstallationsService projectInstallationsService;
	public final TreeGrid<SiteTreeGridModel> grid;
	public final String projectId;

	SitesView(UserSiteAccessService userSiteAccessService, ProjectInstallationsService projectInstallationsService) {
		this.userSiteAccessService = userSiteAccessService;
		this.projectInstallationsService = projectInstallationsService;
		this.grid = new DenseTreeGrid<>();
		this.projectId = getCurrentResourceId();
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

	private Set<SiteTreeGridModel> loadNextLevelData(UsersSitesAccesses usersSitesAccesses, SiteTreeGridModel model) {
		if(model.userId != null)
			return Set.of();
		String siteId = model.siteId;
		return  usersSitesAccesses.getUsersInstalledOnSite().stream()
			.map(furmsUser -> SiteTreeGridModel.builder()
				.siteId(siteId)
				.userId(furmsUser.fenixUserId.get())
				.userName(furmsUser.firstName.orElse("") + " " + furmsUser.lastName.orElse(""))
				.userEmail(furmsUser.email)
				.userAccessStatus(usersSitesAccesses.getStatus(siteId, furmsUser.fenixUserId.get()))
				.build())
			.collect(Collectors.toSet());
	}

	private void fillGrid() {
		grid.addHierarchyColumn(model -> Optional.ofNullable(model.siteName).orElse(""))
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
		grid.addColumn(model -> Optional.ofNullable(model.userAccessStatus)
				.map(status -> status.status.isEnabled() ?
					getTranslation("view.project-admin.sites.enabled") : getTranslation("view.project-admin.sites.disabled"))
				.orElse("")
		)
			.setHeader(getTranslation("view.project-admin.sites.grid.5"))
			.setSortable(true)
			.setFlexGrow(25);
		grid.addComponentColumn(model -> Optional.ofNullable(model.userAccessStatus)
			.map(status -> (Component) new StatusLayout(
				getTranslation("view.project-admin.sites." + status.status.name()),
				status.message,
				getContent()))
			.orElse(new Div())
		)
			.setHeader(getTranslation("view.project-admin.sites.grid.6"))
			.setSortable(true)
			.setFlexGrow(25);
		grid.addComponentColumn(resourceAccessModel -> {
			if(resourceAccessModel.userAccessStatus == null || resourceAccessModel.userAccessStatus.status.isPending()) {
				IconButton iconButton = new IconButton(REFRESH.create());
				iconButton.addClickListener(event -> loadGridContent());
				return iconButton;
			}
			else {
				GridActionMenu contextMenu = new GridActionMenu();
				if(resourceAccessModel.userAccessStatus.status.isInstalled()) {
					contextMenu.addItem(new MenuButton(
							getTranslation("view.project-admin.sites.revoke"), MINUS_CIRCLE),
						event -> {
						userSiteAccessService.removeAccess(resourceAccessModel.siteId, projectId, resourceAccessModel.userId);
						loadGridContent();
					});
				}
				else {
					contextMenu.addItem(new MenuButton(
							getTranslation("view.project-admin.sites.grant"), PLUS_CIRCLE),
						event -> {
							userSiteAccessService.addAccess(resourceAccessModel.siteId, projectId, resourceAccessModel.userId);
							loadGridContent();
						});
				}
				return new GridActionsButtonLayout(contextMenu.getTarget());
			}

		})
			.setHeader(getTranslation("view.project-admin.sites.grid.7"))
			.setTextAlign(ColumnTextAlign.END);

		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
		loadGridContent();
	}

	private void loadGridContent() {
		UsersSitesAccesses usersSitesAccesses = userSiteAccessService.getUsersSitesAccesses(projectId);
		Set<SiteTreeGridModel> rootItems = loadData();
		Set<SiteTreeGridModel> currentExpandedItems = rootItems.stream()
			.filter(grid::isExpanded)
			.collect(Collectors.toSet());

		grid.setItems(rootItems, key -> loadNextLevelData(usersSitesAccesses, key));
		grid.expand(currentExpandedItems);
	}
}
