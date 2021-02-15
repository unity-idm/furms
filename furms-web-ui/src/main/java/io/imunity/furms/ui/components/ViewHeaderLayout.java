/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

@CssImport("./styles/components/view-header.css")
public class ViewHeaderLayout extends HorizontalLayout {


	public ViewHeaderLayout(String headerLabel) {
		this(headerLabel, new Div());
	}
	
	public ViewHeaderLayout(String headerLabel, Component actionComponent) {
		
		setDefault();
		
		HorizontalLayout buttonLayout = new HorizontalLayout(actionComponent);
		buttonLayout.setWidthFull();
		buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		buttonLayout.setAlignItems(FlexComponent.Alignment.END);
		
		H4 title = new H4(headerLabel);
		title.setWidthFull();
		
		add(title, buttonLayout);
	}

	private void setDefault() {
		addClassName("view-header");
		setSizeFull();
	}
}
