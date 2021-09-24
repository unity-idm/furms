/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.sites;

import static com.vaadin.flow.component.icon.VaadinIcon.REFRESH;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.project_installation.ProjectInstallationsService;
import io.imunity.furms.domain.project_installation.ProjectUpdateJobStatus;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.IconButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.StatusLayout;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

@Route(value = "project/admin/sites", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.sites.page.title")
public class SitesView extends FurmsViewComponent {
	public final ProjectInstallationsService projectInstallationsService;
	public final Grid<SiteGridModel> grid;

	SitesView(ProjectInstallationsService projectInstallationsService) {
		this.projectInstallationsService = projectInstallationsService;
		this.grid = new SparseGrid<>(SiteGridModel.class);
		fillGrid();
		getContent().add(new ViewHeaderLayout(getTranslation("view.community-admin.projects.header")), grid);
	}

	private Set<SiteGridModel> loadData() {
		Map<String, ProjectUpdateJobStatus> collect = projectInstallationsService.findAllUpdatesByProjectId(getCurrentResourceId()).stream()
			.collect(Collectors.toMap(x -> x.siteId, x -> x));
		return projectInstallationsService.findAllByProjectId(getCurrentResourceId()).stream()
			.map(statusJob -> {
				String status = Optional.ofNullable(collect.get(statusJob.siteId))
					.map(x -> getTranslation("project.update.status." + x.status.getPersistentId()))
					.orElse(getTranslation("project.installation.status." + statusJob.status.getPersistentId()));
				String message = statusJob.errorMessage
					.map(y -> y.message)
					.orElse(null);
				return new SiteGridModel(statusJob.siteId, statusJob.siteName, status, message);
			})
			.collect(Collectors.toSet());
	}

	private void fillGrid() {
		grid.addColumn(model -> model.siteName)
			.setHeader(getTranslation("view.project-admin.sites.grid.1"))
			.setSortable(true)
			.setFlexGrow(25);
		grid.addComponentColumn(model -> new StatusLayout(model.status, model.message, getContent()))
			.setHeader(getTranslation("view.project-admin.sites.grid.2"))
			.setSortable(true)
			.setFlexGrow(6);
		grid.addComponentColumn(resourceAccessModel -> {
			IconButton iconButton = new IconButton(REFRESH.create());
			iconButton.addClickListener(event -> grid.setItems(loadData()));
			return iconButton;
		})
			.setHeader(getTranslation("view.project-admin.sites.grid.3"));


		grid.setItems(loadData());
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
	}
}
