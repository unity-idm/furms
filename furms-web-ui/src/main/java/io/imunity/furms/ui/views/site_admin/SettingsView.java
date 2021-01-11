/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site_admin;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

@Route(value = "site/admin/settings", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.settings.page.title")
public class SettingsView extends FurmsViewComponent {
}
