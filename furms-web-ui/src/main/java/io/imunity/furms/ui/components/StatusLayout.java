/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.componentfactory.Tooltip;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import static com.vaadin.flow.component.icon.VaadinIcon.WARNING;

public class StatusLayout extends HorizontalLayout {

	public StatusLayout(String status, String message) {
		if(status != null) {
			Text text = new Text(status);
			add(text);
		}
		if(message != null){
			Tooltip tooltip = new Tooltip();
			Icon icon = WARNING.create();
			tooltip.attachToComponent(icon);
			tooltip.add(message);
			add(tooltip, icon);
		}
	}
}
