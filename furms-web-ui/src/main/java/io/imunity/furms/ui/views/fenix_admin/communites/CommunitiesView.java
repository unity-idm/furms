/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites;

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
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.ui.views.components.BreadCrumbParameter;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.communites.model.CommunityViewModel;
import io.imunity.furms.ui.views.fenix_admin.communites.model.CommunityViewModelMapper;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.vaadin.flow.component.icon.VaadinIcon.*;
import static io.imunity.furms.ui.views.fenix_admin.communites.CommunityConst.*;
import static java.util.stream.Collectors.toSet;

@Route(value = "fenix/admin/communities", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.communities.page.title")
public class CommunitiesView extends FurmsViewComponent {
	private final CommunityService communityService;
	private final Grid<CommunityViewModel> grid;

	CommunitiesView(CommunityService communityService) {
		this.communityService = communityService;

		HorizontalLayout header = createHeaderLayout();
		grid = createCommunityGrid();
		loadGridContent();

		getContent().add(header, new HorizontalLayout(grid));
	}

	private HorizontalLayout createHeaderLayout() {
		Button addButton = new Button(getTranslation("view.fenix-admin.communities.button.add"), PLUS_CIRCLE.create());
		addButton.addClickListener(x -> UI.getCurrent().navigate(CommunityFormView.class));

		HorizontalLayout buttonLayout = new HorizontalLayout(addButton);
		buttonLayout.setWidthFull();
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		buttonLayout.setAlignItems(FlexComponent.Alignment.END);

		H4 headerText = new H4(getTranslation("view.fenix-admin.communities.header"));
		HorizontalLayout header = new HorizontalLayout(headerText, buttonLayout);
		header.setSizeFull();
		return header;
	}

	private Grid<CommunityViewModel> createCommunityGrid() {
		Grid<CommunityViewModel> grid = new Grid<>(CommunityViewModel.class, false);
		grid.setHeightByRows(true);

		grid.addComponentColumn(c -> new RouterLink(c.getName(), CommunityView.class, c.getId()))
			.setHeader(getTranslation("view.fenix-admin.communities.grid.column.1"));
		grid.addColumn(CommunityViewModel::getDescription)
			.setHeader(getTranslation("view.fenix-admin.communities.grid.column.2"));
		grid.addComponentColumn(this::createLastColumnContent)
			.setHeader(getTranslation("view.fenix-admin.communities.grid.column.3"))
			.setTextAlign(ColumnTextAlign.END);

		return grid;
	}

	private HorizontalLayout createLastColumnContent(CommunityViewModel c) {
		HorizontalLayout horizontalLayout = new HorizontalLayout(
			createRouterIcon(USERS, c.getId(), ADMINISTRATORS_PARAM),
			createRouterIcon(PIE_CHART, c.getId(), ALLOCATIONS_PARAM),
			createContextMenu(c.getId())
		);
		horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		return horizontalLayout;
	}

	private void loadGridContent() {
		Set<CommunityViewModel> all = communityService.findAll().stream()
			.map(CommunityViewModelMapper::map)
			.collect(toSet());
		grid.setItems(all);
	}

	private Component createContextMenu(String communityId) {
		Icon menu = MENU.create();
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setOpenOnClick(true);
		contextMenu.setTarget(menu);

		contextMenu.addItem(createMenuButton(getTranslation("view.fenix-admin.communities.menu.edit"), EDIT), event ->
			UI.getCurrent().navigate(CommunityFormView.class, communityId)
		);
		contextMenu.addItem(createMenuButton(getTranslation("view.fenix-admin.communities.menu.delete"), TRASH), event -> {
			communityService.delete(communityId);
			loadGridContent();
			}
		);

		Component adminComp = createMenuButton(getTranslation("view.fenix-admin.communities.menu.administrators"), USERS);
		RouterLink administratorsPool = createRouterPool(adminComp, communityId, ADMINISTRATORS_PARAM);
		contextMenu.addItem(administratorsPool);

		Component allocationComp = createMenuButton(getTranslation("view.fenix-admin.communities.menu.allocations"), PIE_CHART);
		RouterLink allocationsPool = createRouterPool(allocationComp, communityId, ALLOCATIONS_PARAM);
		contextMenu.addItem(allocationsPool);

		getContent().add(contextMenu);
		return menu;
	}

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

	public Optional<BreadCrumbParameter> getParameter(){
		return Optional.of(new BreadCrumbParameter("", getTranslation("view.fenix-admin.communities.breadcrumb")));
	}
}
