/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

import static io.imunity.furms.domain.constant.RoutesConst.PROJECT_BASE_LANDING_PAGE;

@Route(value = PROJECT_BASE_LANDING_PAGE, layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.users.page.title")
public class UsersView extends FurmsViewComponent {
}
