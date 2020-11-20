/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.views.login;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Route("login")
@PageTitle("Login")
public class LoginScreen extends VerticalLayout
{
	LoginScreen(@Value("${placeholder.login}") String loginTxt)
	{
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.getThemeList().set("spacing-s", true);
		layout.setAlignItems(Alignment.CENTER);
		layout.setJustifyContentMode(JustifyContentMode.CENTER);
		Anchor login = new Anchor("/oauth2/authorization/unity", loginTxt);
		layout.add(login);
		add(layout);
		setSizeFull();
	}
}