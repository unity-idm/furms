/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class GridActionsButtonLayout extends HorizontalLayout {

	public GridActionsButtonLayout(Component... children) {
		super(children);
		setSpacing(false);
		setJustifyContentMode(FlexComponent.JustifyContentMode.END);
	}

}
