/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.sites;

import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;
import io.imunity.furms.ui.views.project.resource_access.ResourceAccessModel;

import static com.vaadin.flow.component.icon.VaadinIcon.CHEVRON_DOWN_SMALL;
import static com.vaadin.flow.component.icon.VaadinIcon.CHEVRON_RIGHT_SMALL;
import static java.util.Collections.emptyList;

@Route(value = "project/admin/sites", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.sites.page.title")
public class SitesView extends FurmsViewComponent {
	public final TreeGrid<ResourceAccessModel> treeGrid;


	SitesView() {
		this.treeGrid = new TreeGrid<>();
		fillTreeGrid();
	}

	private void fillTreeGrid() {
		GridActionMenu contextMenu2 = new GridActionMenu();
		contextMenu2.addItem(new MenuButton(
				getTranslation("view.project-admin.resource-access.grid.context-menu.expand"), CHEVRON_DOWN_SMALL),
			event -> treeGrid.expand(resourceAccessViewService.getData().keySet())
		);
		contextMenu2.addItem(new MenuButton(
				getTranslation("view.project-admin.resource-access.grid.context-menu.collapse"), CHEVRON_RIGHT_SMALL),
			event -> treeGrid.collapse(resourceAccessViewService.getData().keySet())
		);
		treeGrid.addHierarchyColumn(ResourceAccessModel::getFullName)
			.setHeader(new HorizontalLayout(contextMenu2.getTarget(),
				new Label(getTranslation("view.project-admin.resource-access.grid.column.1"))))
			.setSortable(true)
			.setFlexGrow(25);
		treeGrid.addColumn(ResourceAccessModel::getEmail)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.3"))
			.setSortable(true)
			.setFlexGrow(25);
		treeGrid.addColumn(ResourceAccessModel::getAllocation)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.4"))
			.setSortable(true)
			.setFlexGrow(25);
		treeGrid.addColumn(ResourceAccessModel::getAccess)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.5"))
			.setSortable(true)
			.setFlexGrow(12);
		treeGrid.addComponentColumn(this::getStatusLayout)
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.6"))
			.setSortable(true)
			.setFlexGrow(13);
		treeGrid.addComponentColumn(resourceAccessModel -> {
			if(isRootNode(resourceAccessModel) || resourceAccessModel.isAccessible())
				return new Div();
			if(resourceAccessViewService.isGrantOrRevokeAvailable(resourceAccessModel))
				return getGridActionsButtonLayout(resourceAccessModel);
			return getGridRefreshButtonLayout();
		})
			.setHeader(getTranslation("view.project-admin.resource-access.grid.column.7"))
			.setWidth("6em");
		treeGrid.setItems(resourceAccessViewService.getData().keySet(), key -> resourceAccessViewService.getData().getOrDefault(key, emptyList()));
		treeGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
	}
}
