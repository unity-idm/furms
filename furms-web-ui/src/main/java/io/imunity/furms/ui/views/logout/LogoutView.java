/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.logout;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

import static io.imunity.furms.domain.constant.LoginFlowConst.AUTH_REQ_BASE_URL;

@Route("public/logout")
@PageTitle(key = "view.logout-page.title")
public class LogoutView extends FurmsViewComponent {
	LogoutView() {
		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();
		layout.setPadding(false);
		layout.setSpacing(false);
		layout.getThemeList().set("spacing-s", true);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		Span span = new Span(getTranslation("view.logout-page.message"));
		Anchor login = new Anchor(AUTH_REQ_BASE_URL, getTranslation("view.login-page.login"));
		layout.add(span, login);
		getContent().add(layout);
		getContent().setSizeFull();
	}
}