/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.views.login;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.util.List;
import java.util.Map;

import static io.imunity.furms.constant.LoginFlowConst.*;

@Route("login")
@PageTitle("Login")
public class LoginScreen extends VerticalLayout implements HasUrlParameter<String>
{
	private Anchor login;

	public LoginScreen()
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.getThemeList().set("spacing-s", true);
		layout.setAlignItems(Alignment.CENTER);
		layout.setJustifyContentMode(JustifyContentMode.CENTER);
		login = new Anchor("", "Login with Unity");
		layout.add(login);
		add(layout);
		setSizeFull();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		Location location = event.getLocation();
		QueryParameters queryParameters = location.getQueryParameters();
		Map<String, List<String>> parametersMap = queryParameters.getParameters();
		login.setHref((parametersMap.containsKey("dev") ? AUTH_REQ_BASE_URL : AUTH_REQ_PARAM_URL) + REGISTRATION_ID);
	}
}