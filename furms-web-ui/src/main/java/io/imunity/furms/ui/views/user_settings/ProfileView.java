/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

import static io.imunity.furms.domain.constant.RoutesConst.USER_BASE_LANDING_PAGE;

@Route(value = USER_BASE_LANDING_PAGE, layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.profile.page.title")
public class ProfileView extends FurmsViewComponent {
}
