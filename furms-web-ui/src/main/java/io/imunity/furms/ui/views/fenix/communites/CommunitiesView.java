/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites;

import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PIE_CHART;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.icon.VaadinIcon.USERS;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static io.imunity.furms.ui.views.fenix.communites.CommunityConst.ADMINISTRATORS_PARAM;
import static io.imunity.furms.ui.views.fenix.communites.CommunityConst.ALLOCATIONS_PARAM;
import static io.imunity.furms.ui.views.fenix.communites.CommunityConst.PARAM_NAME;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.ui.community.CommunityViewModel;
import io.imunity.furms.ui.community.CommunityViewModelMapper;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.GridActionMenu;
import io.imunity.furms.ui.components.GridActionsButtonLayout;
import io.imunity.furms.ui.components.MenuButton;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.RouterGridLink;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

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

		return new ViewHeaderLayout(getTranslation("view.fenix-admin.communities.header"), addButton);
	}

	private Grid<CommunityViewModel> createCommunityGrid() {
		Grid<CommunityViewModel> grid = new SparseGrid<>(CommunityViewModel.class);

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
		return new GridActionsButtonLayout(
			new RouterGridLink(USERS, c.getId(), CommunityView.class, PARAM_NAME, ADMINISTRATORS_PARAM),
			new RouterGridLink(PIE_CHART, c.getId(), CommunityView.class, PARAM_NAME, ALLOCATIONS_PARAM),
			createContextMenu(c.getId())
		);
	}

	private void loadGridContent() {
		Set<CommunityViewModel> all = handleExceptions(communityService::findAll)
			.orElseGet(Collections::emptySet)
			.stream()
			.map(CommunityViewModelMapper::map)
			.collect(toSet());
		grid.setItems(all);
	}

	private Component createContextMenu(String communityId) {
		GridActionMenu contextMenu = new GridActionMenu();

		contextMenu.addItem(new MenuButton(getTranslation("view.fenix-admin.communities.menu.edit"), EDIT), event ->
			UI.getCurrent().navigate(CommunityFormView.class, communityId)
		);
		contextMenu.addItem(new MenuButton(getTranslation("view.fenix-admin.communities.menu.delete"), TRASH), event -> {
			handleExceptions(() -> communityService.delete(communityId));
			loadGridContent();
			}
		);

		MenuButton adminComp = new MenuButton(getTranslation("view.fenix-admin.communities.menu.administrators"), USERS);
		RouterLink administratorsPool = new RouterGridLink(adminComp, communityId, CommunityView.class, PARAM_NAME, ADMINISTRATORS_PARAM);
		contextMenu.addItem(administratorsPool);

		MenuButton allocationComp = new MenuButton(getTranslation("view.fenix-admin.communities.menu.allocations"), PIE_CHART);
		RouterLink allocationsPool = new RouterGridLink(allocationComp, communityId, CommunityView.class, PARAM_NAME, ALLOCATIONS_PARAM);
		contextMenu.addItem(allocationsPool);

		getContent().add(contextMenu);
		return contextMenu.getTarget();
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter(){
		return Optional.of(new BreadCrumbParameter("", getTranslation("view.fenix-admin.communities.breadcrumb")));
	}
}
