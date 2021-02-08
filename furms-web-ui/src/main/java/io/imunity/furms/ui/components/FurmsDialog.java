/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class FurmsDialog extends Dialog {
	private final Button confirmButton = new Button(getTranslation("view.community-admin.projects.dialog.button.approve"));
	private final Button cancelButton = new Button(getTranslation("view.community-admin.projects.dialog.button.cancel"));

	public FurmsDialog(String label) {
		confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		confirmButton.addClickListener(event -> close());

		cancelButton.addClickListener(event -> close());
		cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		HorizontalLayout horizontalLayout = new HorizontalLayout(cancelButton, confirmButton);
		horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		horizontalLayout.setWidthFull();
		add(
			new VerticalLayout(
				new Span(label),
				horizontalLayout
			)
		);
	}

	public void addConfirmButtonClickListener(ComponentEventListener<ClickEvent<Button>> listener){
		confirmButton.addClickListener(listener);
	}

	public void addCancelButtonClickListener(ComponentEventListener<ClickEvent<Button>> listener){
		cancelButton.addClickListener(listener);
	}
}
