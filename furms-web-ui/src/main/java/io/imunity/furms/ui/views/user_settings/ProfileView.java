/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import static io.imunity.furms.domain.constant.RoutesConst.USER_BASE_LANDING_PAGE;

import java.util.Map;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.domain.constant.CommonAttribute;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SingleColumnFormLayout;

@Route(value = USER_BASE_LANDING_PAGE, layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.profile.page.title")
public class ProfileView extends FurmsViewComponent {

	public ProfileView(AuthzService authzService) {
		Map<String, Object> userAttributes = authzService.getAttributes();

		SingleColumnFormLayout mainLayout = new SingleColumnFormLayout();
		mainLayout.setSizeFull();
		
		String firstname = get(userAttributes, CommonAttribute.FIRSTNAME.name);
		String surname = get(userAttributes, CommonAttribute.SURNAME.name);
		String email = get(userAttributes, CommonAttribute.EMAIL.name);

		mainLayout.addFormItem(new Label(firstname),
				getTranslation("view.user-settings.profile.firstname"));
		mainLayout.addFormItem(new Label(surname),
				getTranslation("view.user-settings.profile.surname"));
		mainLayout.addFormItem(new Label(email),
				getTranslation("view.user-settings.profile.email"));

		getContent().add(mainLayout);
	}
	
	private String get(Map<String, Object> userAttributes, String key) {
		Object value = userAttributes.get(key);
		if (value == null)
			return "";
		return value.toString();
	}
}
