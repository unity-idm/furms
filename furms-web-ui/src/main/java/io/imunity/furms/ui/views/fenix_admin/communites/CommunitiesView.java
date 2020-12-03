/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;


@Route(value = "fenix/admin/communities", layout = FenixAdminMenu.class)
@PageTitle(key = "view.communities.page.title")
public class CommunitiesView extends FurmsViewComponent {
	CommunitiesView(CommunityService communityService) {
		Grid<Community> grid = new Grid<>(Community.class, false);
		grid.setHeightByRows(true);
		grid.setItems(communityService.findAll());

		grid.addComponentColumn(c -> new RouterLink(c.getUserFacingName(), CommunityView.class, c.getId())).setHeader("Name");
		grid.addColumn(Community::getDescription).setHeader("Description");
		getContent().add(grid);
	}
}
