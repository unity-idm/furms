/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

public class MenuComponentFactory {
	public static Component createMenuButton(String label, VaadinIcon icon) {
		Span text = new Span(label);
		Div div = new Div(createMenuIcon(icon), text);
		div.addClassName("menu-div");
		return div;
	}

	public static Icon createMenuIcon(VaadinIcon iconType) {
		Icon icon = iconType.create();
		icon.addClassNames("menu-icon-padding");
		return icon;
	}
}
