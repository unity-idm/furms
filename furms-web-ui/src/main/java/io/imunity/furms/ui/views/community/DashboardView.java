/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

import static io.imunity.furms.domain.constant.RoutesConst.COMMUNITY_BASE_LANDING_PAGE;

@Route(value = COMMUNITY_BASE_LANDING_PAGE, layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.dashboard.page.title")
public class DashboardView extends FurmsViewComponent {
}
