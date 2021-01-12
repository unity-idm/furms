/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project_admin;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

@Route(value = "project/admin/settings", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.settings.page.title")
public class SettingsView extends FurmsViewComponent {
}
