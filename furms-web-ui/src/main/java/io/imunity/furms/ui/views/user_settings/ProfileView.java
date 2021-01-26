/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import static io.imunity.furms.domain.constant.RoutesConst.USER_BASE_LANDING_PAGE;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.base.Strings;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.Route;

import io.imunity.furms.core.config.security.user.FurmsUser;
import io.imunity.furms.domain.constant.AttributesConst;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

@Route(value = USER_BASE_LANDING_PAGE, layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.profile.page.title")
public class ProfileView extends FurmsViewComponent {

	public ProfileView() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		FurmsUser user = (FurmsUser) authentication.getPrincipal();

		FormLayout mainLayout = new FormLayout();
		mainLayout.setSizeFull();
		mainLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("1em", 1));
		String firstname = user.getAttribute(AttributesConst.FIRSTNAME);
		String surname = user.getAttribute(AttributesConst.SURNAME);
		String email = user.getAttribute(AttributesConst.EMAIL);

		mainLayout.addFormItem(new Label(Strings.isNullOrEmpty(firstname) ? "" : firstname),
				getTranslation("view.user-settings.profile.firstname"));
		mainLayout.addFormItem(new Label(Strings.isNullOrEmpty(surname) ? "" : surname),
				getTranslation("view.user-settings.profile.surname"));
		mainLayout.addFormItem(new Label(Strings.isNullOrEmpty(email) ? "" : email),
				getTranslation("view.user-settings.profile.email"));

		getContent().add(mainLayout);
	}
}
