/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;

public class InviteUserComponent extends HorizontalLayout {


	private final Button inviteButton;
	private final TextField email;

	public InviteUserComponent() {
		super();
		setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		setSpacing(true);

		email = new TextField();
		email.setPlaceholder(getTranslation("component.invite.field.placeholder"));

		inviteButton = new Button(getTranslation("component.invite.button"), PAPERPLANE.create());

		add(email, inviteButton);
	}

	public void addInviteAction(ComponentEventListener<ClickEvent<Button>> inviteAction) {
		inviteButton.addClickListener(inviteAction);
	}

	public TextField getEmail() {
		return email;
	}
}
