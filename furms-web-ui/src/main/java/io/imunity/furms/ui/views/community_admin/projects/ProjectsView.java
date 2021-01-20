/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community_admin.projects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.ui.views.community_admin.CommunityAdminMenu;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.communites.CommunityView;

import java.util.Map;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.views.community_admin.projects.ProjectConst.*;
import static java.util.stream.Collectors.toSet;

@Route(value = "community/admin/projects", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.projects.page.title")
public class ProjectsView extends FurmsViewComponent {

	private final ProjectService projectService;
	private final Grid<ProjectViewModel> grid;

	public ProjectsView(ProjectService projectService) {
		this.projectService = projectService;
		this.grid = createCommunityGrid();

		loadGridContent();

		getContent().add(createHeaderLayout(), new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout() {
		Button addButton = new Button(getTranslation("view.communities.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(ProjectFormView.class));

		HorizontalLayout buttonLayout = new HorizontalLayout(addButton);
		buttonLayout.setWidthFull();
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		buttonLayout.setAlignItems(FlexComponent.Alignment.END);

		H4 headerText = new H4(getTranslation("view.communities.header"));
		HorizontalLayout header = new HorizontalLayout(headerText, buttonLayout);
		header.setSizeFull();
		return header;
	}

	private Grid<ProjectViewModel> createCommunityGrid() {
		Grid<ProjectViewModel> grid = new Grid<>(ProjectViewModel.class, false);
		grid.setHeightByRows(true);

		grid.addComponentColumn(c -> new RouterLink(c.name, ProjectsView.class, c.id))
			.setHeader(getTranslation("view.communities.grid.column.1"));
		grid.addColumn(c -> c.description)
			.setHeader(getTranslation("view.communities.grid.column.2"));
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.communities.grid.column.3"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(ProjectViewModel c) {
		HorizontalLayout horizontalLayout = new HorizontalLayout(
			createRouterIcon(USERS, c.id, ADMINISTRATORS_PARAM),
			createRouterIcon(PIE_CHART, c.id, ALLOCATIONS_PARAM),
			createContextMenu(c.id, c.communityId)
		);
		horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return horizontalLayout;
	}

	private Component createContextMenu(String projectId, String communityId) {
		Icon menu = MENU.create();
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setOpenOnClick(true);
		contextMenu.setTarget(menu);

		contextMenu.addItem(createMenuButton(getTranslation("view.communities.menu.edit"), EDIT), event ->
			UI.getCurrent().navigate(ProjectFormView.class, communityId)
		);
		contextMenu.addItem(createMenuButton(getTranslation("view.communities.menu.delete"), TRASH), event -> {
			projectService.delete(projectId, communityId);
				loadGridContent();
			}
		);

		Component adminComp = createMenuButton(getTranslation("view.communities.menu.administrators"), USERS);
		RouterLink administratorsPool = createRouterPool(adminComp, communityId, ADMINISTRATORS_PARAM);
		contextMenu.addItem(administratorsPool);

		Component allocationComp = createMenuButton(getTranslation("view.communities.menu.allocations"), PIE_CHART);
		RouterLink allocationsPool = createRouterPool(allocationComp, communityId, ALLOCATIONS_PARAM);
		contextMenu.addItem(allocationsPool);

		getContent().add(contextMenu);
		return menu;
	}

	private void loadGridContent() {
		Set<ProjectViewModel> all = projectService.findAll().stream()
			.map(ProjectViewModelMapper::map)
			.collect(toSet());
		grid.setItems(all);
	}

	//TODO EXTRACT IT
	private Component createMenuButton(String label, VaadinIcon icon) {
		Span text = new Span(label);
		Div div = new Div(createMenuIcon(icon), text);
		div.addClassName("menu-div");
		return div;
	}

	private Icon createMenuIcon(VaadinIcon iconType) {
		Icon icon = iconType.create();
		icon.addClassNames("menu-icon-padding");
		return icon;
	}

	private RouterLink createRouterIcon(VaadinIcon iconType, String id, String param) {
		Icon icon = iconType.create();
		return createRouterPool(icon, id, param);
	}

	private RouterLink createRouterPool(Component component, String id, String param) {
		RouterLink routerLink = new RouterLink("", CommunityView.class, id);
		routerLink.setQueryParameters(QueryParameters.simple(Map.of(PARAM_NAME, param)));
		routerLink.add(component);
		routerLink.setClassName("furms-color");
		return routerLink;
	}
}
