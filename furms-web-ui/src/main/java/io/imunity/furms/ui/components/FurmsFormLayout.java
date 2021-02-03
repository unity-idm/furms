/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import static com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition.TOP;

import com.vaadin.flow.component.formlayout.FormLayout;

public class FurmsFormLayout extends FormLayout {

	public FurmsFormLayout() {
		setResponsiveSteps(new FormLayout.ResponsiveStep("1em", 1, TOP));
	}
}
