/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
import com.vaadin.flow.theme.lumo.Lumo;
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

import static com.vaadin.flow.component.icon.VaadinIcon.ALIGN_JUSTIFY;
import static com.vaadin.flow.component.icon.VaadinIcon.EDIT;
import static com.vaadin.flow.component.icon.VaadinIcon.PIE_CHART;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.TRASH;
import static com.vaadin.flow.component.icon.VaadinIcon.USERS;
import static io.imunity.furms.ui.views.fenix_admin.communites.CommunityConst.ADMINISTRATORS_PARAM;
import static io.imunity.furms.ui.views.fenix_admin.communites.CommunityConst.ALLOCATIONS_PARAM;
import static io.imunity.furms.ui.views.fenix_admin.communites.CommunityConst.PARAM_NAME;
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
		addButton.setClassName("furms-color");

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
			createTooltipIcon(c.getId())
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

	private Icon createTooltipIcon(String communityId) {
		Icon icon = ALIGN_JUSTIFY.create();
		Tooltip tooltip = createTooltip(communityId);
		tooltip.attachToComponent(icon);
		getContent().add(tooltip);
		return icon;
	}

	private Tooltip createTooltip(String communityId) {
		Tooltip tooltip = new Tooltip();

		Span editSpan = new Span(getTranslation("view.fenix-admin.communities.tooltip.edit"));
		Div editCommunityDiv = new Div(createTooltipIcon(EDIT), editSpan);
		editCommunityDiv.addClassName("tooltip-div");
		editCommunityDiv.addClickListener(x -> UI.getCurrent().navigate(CommunityFormView.class, communityId));

		Span deleteSpan = new Span(getTranslation("view.fenix-admin.communities.tooltip.delete"));
		Div deleteCommunityDiv = new Div(createTooltipIcon(TRASH), deleteSpan);
		deleteCommunityDiv.addClassName("tooltip-div");
		deleteCommunityDiv.addClickListener(x -> {
			communityService.delete(communityId);
			loadGridContent();
			tooltip.close();
		});

		Span administratorsSpan = new Span(getTranslation("view.fenix-admin.communities.tooltip.administrators"));
		Div administratorsDiv = new Div(createTooltipIcon(USERS), administratorsSpan);
		administratorsDiv.addClassName("tooltip-div");
		RouterLink administratorsPool = createRouterPool(administratorsDiv, communityId, ADMINISTRATORS_PARAM);

		Span allocationSpan = new Span(getTranslation("view.fenix-admin.communities.tooltip.allocations"));
		Div allocationsDiv = new Div(createTooltipIcon(PIE_CHART), allocationSpan);
		allocationsDiv.addClassName("tooltip-div");
		RouterLink allocationsPool = createRouterPool(allocationsDiv, communityId, ALLOCATIONS_PARAM);

		tooltip.add(editCommunityDiv, deleteCommunityDiv, administratorsPool, allocationsPool);
		tooltip.getThemeNames().add(Lumo.LIGHT);
		return tooltip;
	}

	private Icon createTooltipIcon(VaadinIcon iconType) {
		Icon icon = iconType.create();
		icon.addClassNames("tooltip-icon-padding");
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
