/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;

public class MenuComponentFactory {

	public static Component createActionButton(String label, VaadinIcon icon) {
		Button button;
		if (label == null)
			button = new Button(icon.create());
		else
			button = new Button(label, icon.create());
		button.addThemeVariants(LUMO_TERTIARY);
		button.setClassName("action-button");
		return button;
	}
	
	public static Component createActionButton(VaadinIcon icon) {
		return createActionButton(null, icon);
	}
}
