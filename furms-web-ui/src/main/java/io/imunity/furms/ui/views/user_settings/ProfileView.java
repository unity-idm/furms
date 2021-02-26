/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SingleColumnFormLayout;

import static io.imunity.furms.domain.constant.RoutesConst.USER_BASE_LANDING_PAGE;
import static java.util.Optional.ofNullable;

@Route(value = USER_BASE_LANDING_PAGE, layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.profile.page.title")
public class ProfileView extends FurmsViewComponent {

	public ProfileView(AuthzService authzService) {
		FURMSUser user = authzService.getCurrentAuthNUser();

		SingleColumnFormLayout mainLayout = new SingleColumnFormLayout();
		mainLayout.setSizeFull();

		mainLayout.addFormItem(new Label(ofNullable(user.firstName).orElse("")),
				getTranslation("view.user-settings.profile.firstname"));
		mainLayout.addFormItem(new Label(ofNullable(user.lastName).orElse("")),
				getTranslation("view.user-settings.profile.surname"));
		mainLayout.addFormItem(new Label(user.email),
				getTranslation("view.user-settings.profile.email"));

		getContent().add(mainLayout);
	}
}
