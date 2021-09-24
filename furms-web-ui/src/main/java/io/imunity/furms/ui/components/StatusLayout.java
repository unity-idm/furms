/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import static com.vaadin.flow.component.icon.VaadinIcon.WARNING;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class StatusLayout extends HorizontalLayout {

	public StatusLayout() {

	}

	public StatusLayout(String status, String message, HtmlContainer tooltipAttachment) {
		Div content = new Div();
		if (status != null) {
			content.add(new Text(status));
		}
		if (message != null) {
			Tooltip tooltip = new Tooltip();
			Icon icon = WARNING.create();
			tooltip.attachToComponent(icon);
			tooltipAttachment.add(tooltip);
			tooltip.add(message);
			
			icon.getStyle().set("margin-left", "5px");
			content.add(icon);
		}
		add(content);
		setMargin(false);
		setSpacing(false);
		setAlignItems(Alignment.CENTER);
	}
}
