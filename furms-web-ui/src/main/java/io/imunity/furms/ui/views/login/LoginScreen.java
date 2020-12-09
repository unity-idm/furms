/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.login;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.util.List;
import java.util.Map;

import static io.imunity.furms.domain.constant.LoginFlowConst.*;

@Route("public/login")
@PageTitle("Login")
public class LoginScreen extends Composite<Div> implements HasUrlParameter<String> {
	private final Anchor login;

	LoginScreen() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.getThemeList().set("spacing-s", true);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		login = new Anchor("", getTranslation("view.login-page.login"));
		layout.add(login);
		getContent().add(layout);
		getContent().setSizeFull();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		Location location = event.getLocation();
		QueryParameters queryParameters = location.getQueryParameters();
		Map<String, List<String>> parametersMap = queryParameters.getParameters();
		login.setHref((parametersMap.containsKey("dev") ? AUTH_REQ_BASE_URL : AUTH_REQ_PARAM_URL) + REGISTRATION_ID);
	}
}