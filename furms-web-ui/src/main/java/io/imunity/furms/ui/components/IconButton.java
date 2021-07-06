/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.icon.Icon;

@CssImport("./styles/components/icon-button.css")
public class IconButton extends Button {

	public IconButton(Icon icon) {
		super(icon);
		icon.setClassName("furms-icon");
		addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		getStyle().set("opacity", "1");
		getStyle().set("cursor", "pointer");
	}
}
