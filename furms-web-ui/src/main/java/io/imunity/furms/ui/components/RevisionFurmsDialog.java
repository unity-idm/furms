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

public class RevisionFurmsDialog extends Dialog {
	private final Button revisionButton = new Button(getTranslation("component.revision.furms.dialog.save.with.revision"));
	private final Button saveButton = new Button(getTranslation("component.revision.furms.dialog.save.silently"));
	private final Button cancelButton = new Button(getTranslation("component.revision.furms.dialog.cancel"));

	public RevisionFurmsDialog(String label) {
		revisionButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		revisionButton.addClickListener(event -> close());

		saveButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		saveButton.addClickListener(event -> close());

		cancelButton.addClickListener(event -> close());
		cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

		HorizontalLayout horizontalLayout = new HorizontalLayout(cancelButton, saveButton, revisionButton);
		horizontalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		horizontalLayout.setWidthFull();
		add(
			new VerticalLayout(
				new Span(label),
				horizontalLayout
			)
		);
		setMaxWidth("60em");
	}

	public void addSaveButtonClickListener(ComponentEventListener<ClickEvent<Button>> listener){
		saveButton.addClickListener(listener);
	}

	public void addSaveWithRevisionButtonClickListener(ComponentEventListener<ClickEvent<Button>> listener){
		revisionButton.addClickListener(listener);
	}

	public void addCancelButtonClickListener(ComponentEventListener<ClickEvent<Button>> listener){
		cancelButton.addClickListener(listener);
	}
}
