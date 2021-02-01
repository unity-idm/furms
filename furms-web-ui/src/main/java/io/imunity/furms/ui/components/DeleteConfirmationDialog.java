/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;

public class DeleteConfirmationDialog extends Dialog {

	public DeleteConfirmationDialog(String message, ComponentEventListener<ClickEvent<Button>> actionOnDelete) {
		super();
		setWidth("30em");

		Label messageLabel = new Label(message);
		messageLabel.setSizeFull();

		Button cancel = new Button(getTranslation("components.confirmation.dialog.button.cancel"), event -> close());
		cancel.addThemeVariants(LUMO_TERTIARY);

		Button save = new Button(getTranslation("components.confirmation.dialog.button.delete"), actionOnDelete);
		save.addClickListener(event -> close());
		save.addThemeVariants(LUMO_PRIMARY);
		save.addClickShortcut(Key.ENTER);

		VerticalLayout layout = new VerticalLayout(messageLabel, new FormButtons(cancel, save));
		layout.setAlignItems(FlexComponent.Alignment.END);
		add(layout);
	}
}
