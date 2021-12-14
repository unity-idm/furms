/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

@CssImport("./styles/components/menu-button.css")
public class MenuButton extends Div {

	public MenuButton(String label, VaadinIcon icon) {
		super(icon.create(), new Span(label));
		addClassName("menu-button");
	}
	
	public MenuButton(VaadinIcon icon) {
		this(null, icon);
	}
}
