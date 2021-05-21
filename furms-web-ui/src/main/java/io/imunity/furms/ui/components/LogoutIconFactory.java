/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import static io.imunity.furms.domain.constant.RoutesConst.LOGOUT_TRIGGER_URL;

public class LogoutIconFactory {
	public static Icon create(){
		Icon logout = new Icon(VaadinIcon.SIGN_OUT);
		logout.getStyle().set("cursor", "pointer");
		logout.addClickListener(
			event -> UI.getCurrent().getPage().setLocation(LOGOUT_TRIGGER_URL)
		);
		return logout;
	}
}
