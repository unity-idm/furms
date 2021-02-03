/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.google.common.base.Strings;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.constant.CommonAttribute;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SingleColumnFormLayout;

import java.util.Map;

import static io.imunity.furms.domain.constant.RoutesConst.USER_BASE_LANDING_PAGE;

@Route(value = USER_BASE_LANDING_PAGE, layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.profile.page.title")
public class ProfileView extends FurmsViewComponent {

	public ProfileView(AuthzService authzService) {
		Map<String, Object> userAttributes = authzService.getAttributes();

		SingleColumnFormLayout mainLayout = new SingleColumnFormLayout();
		mainLayout.setSizeFull();
		
		String firstname = userAttributes.get(CommonAttribute.FIRSTNAME.name).toString();
		String surname = userAttributes.get(CommonAttribute.SURNAME.name).toString();
		String email = userAttributes.get(CommonAttribute.EMAIL.name).toString();

		mainLayout.addFormItem(new Label(Strings.isNullOrEmpty(firstname) ? "" : firstname),
				getTranslation("view.user-settings.profile.firstname"));
		mainLayout.addFormItem(new Label(Strings.isNullOrEmpty(surname) ? "" : surname),
				getTranslation("view.user-settings.profile.surname"));
		mainLayout.addFormItem(new Label(Strings.isNullOrEmpty(email) ? "" : email),
				getTranslation("view.user-settings.profile.email"));

		getContent().add(mainLayout);
	}
}
