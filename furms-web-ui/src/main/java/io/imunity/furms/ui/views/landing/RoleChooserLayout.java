/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.landing;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.imunity.furms.ui.components.FurmsSelect;
import io.imunity.furms.ui.components.LogoutIconFactory;
import io.imunity.furms.ui.user_context.RoleTranslator;

class RoleChooserLayout extends VerticalLayout {
	RoleChooserLayout(RoleTranslator roleTranslator) {
		Icon logout = LogoutIconFactory.create();
		HorizontalLayout logoutLayout = new HorizontalLayout(logout);
		logoutLayout.setWidthFull();
		logoutLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

		FurmsSelect furmsSelect = new FurmsSelect(roleTranslator);
		VerticalLayout layout =
			new VerticalLayout(new H4(getTranslation("view.landing.select")), furmsSelect);
		layout.setSizeFull();
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);

		setSizeFull();
		add(logoutLayout, layout);
	}
}
