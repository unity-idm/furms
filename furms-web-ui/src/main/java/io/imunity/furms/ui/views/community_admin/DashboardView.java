/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community_admin;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

@Route(value = "community/admin/dashboard", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.dashboard.page.title")
public class DashboardView extends FurmsViewComponent {
}
