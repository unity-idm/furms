/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.Lumo;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;

import static com.vaadin.flow.component.icon.VaadinIcon.*;

@Route(value = "fenix/admin/communities", layout = FenixAdminMenu.class)
@PageTitle(key = "view.communities.page.title")
public class CommunitiesView extends FurmsViewComponent {
	private final CommunityService communityService;
	private final Grid<Community> grid;
	private final CommunityForm communityForm = new CommunityForm();

	CommunitiesView(CommunityService communityService) {
		this.communityService = communityService;

		grid = new Grid<>(Community.class, false);
		grid.setHeightByRows(true);
		grid.setItems(communityService.findAll());

		grid.addComponentColumn(c -> new RouterLink(c.getName(), CommunityView.class, c.getId())).setHeader("Name");
		grid.addColumn(Community::getDescription).setHeader("Description");


		grid.addComponentColumn(c -> {
			HorizontalLayout horizontalLayout = new HorizontalLayout(
				getRouterIcon(USERS),
				getRouterIcon(PIE_CHART),
				getTooltipIcon(c.getId())
			);
			horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
			return horizontalLayout;
		})
			.setHeader("Actions")
			.setTextAlign(ColumnTextAlign.END);


		Button addButton = new Button("Add", PLUS_CIRCLE.create());
		addButton.addClickListener(x -> communityForm.openEditor(Community.builder().build()));
		HorizontalLayout horizontalLayout = new HorizontalLayout(addButton);
		horizontalLayout.setSizeFull();
		horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		horizontalLayout.setAlignItems(FlexComponent.Alignment.END);

		HorizontalLayout horizontalLayout1 = new HorizontalLayout(new H4("Communities"), horizontalLayout);
		horizontalLayout1.setSizeFull();

		getContent().add(horizontalLayout1);
		getContent().add(new HorizontalLayout(grid, communityForm));
	}

	private Icon getTooltipIcon(String communityId) {
		Icon icon = ALIGN_JUSTIFY.create();
		Tooltip tooltip = getTooltip(communityId);
		tooltip.attachToComponent(icon);
		getContent().add(tooltip);
		return icon;
	}

	private Tooltip getTooltip(String communityId) {
		Tooltip tooltip = new Tooltip();

		Div editCommunityDiv = new Div(getTooltipIcon(EDIT), new Span("Edit"));
		editCommunityDiv.addClassName("tooltip-div");
		editCommunityDiv.addClickListener(x -> communityForm.setContent(communityService.findById(communityId).get()));

		Div deleteCommunityDiv = new Div(getTooltipIcon(TRASH), new Span("Delete"));
		deleteCommunityDiv.addClassName("tooltip-div");
		deleteCommunityDiv.addClickListener(x -> {
			communityService.delete(communityId);
			grid.setItems(communityService.findAll());
			tooltip.close();
		});

		Div administratorsDiv = new Div(getTooltipIcon(USERS), new Span("Administrators"));
		administratorsDiv.addClassName("tooltip-div");
		RouterLink administratorsPool = getRouterPool(administratorsDiv);

		Div allocationsDiv = new Div(getTooltipIcon(PIE_CHART), new Span("Allocations"));
		allocationsDiv.addClassName("tooltip-div");
		RouterLink allocationsPool = getRouterPool(allocationsDiv);

		tooltip.add(editCommunityDiv, deleteCommunityDiv, administratorsPool, allocationsPool);
		tooltip.getThemeNames().add(Lumo.LIGHT);
		return tooltip;
	}

	private Icon getTooltipIcon(VaadinIcon iconType) {
		Icon icon = iconType.create();
		icon.addClassNames("tooltip-icon-padding");
		return icon;
	}

	private RouterLink getRouterIcon(VaadinIcon iconType) {
		Icon icon = iconType.create();
		return getRouterPool(icon);
	}

	private RouterLink getRouterPool(Component component) {
		RouterLink routerLink = new RouterLink("", CommunityView.class);
		routerLink.add(component);
		routerLink.setClassName("furms-color");
		return routerLink;
	}
}
