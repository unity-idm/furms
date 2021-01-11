/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

@Route(value = "users/settings/api/keys", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.api-key.page.title")
public class APIKeyView extends FurmsViewComponent {
}
