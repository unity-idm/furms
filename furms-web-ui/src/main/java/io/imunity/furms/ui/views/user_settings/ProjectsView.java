/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

@Route(value = "users/settings/projects", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.projects.page.title")
public class ProjectsView extends FurmsViewComponent {
}
