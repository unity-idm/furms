/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

@Route(value = "project/admin/alarms", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.alarms.page.title")
public class AlarmsView extends FurmsViewComponent {
}
