/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import static com.vaadin.flow.component.icon.VaadinIcon.WARNING;

public class StatusLayout extends HorizontalLayout {

	public StatusLayout() {

	}

	public StatusLayout(String status, String message) {
		Div content = new Div();
		if (status != null) {
			content.add(new Text(status));
		}
		if (message != null) {
			Icon icon = WARNING.create();
			icon.setTooltipText(message);
			icon.getStyle().set("margin-left", "5px");
			content.add(icon);
		}
		add(content);
		setMargin(false);
		setSpacing(false);
		setAlignItems(Alignment.CENTER);
	}
}
