/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import java.util.function.Supplier;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class MembershipChangerComponent extends HorizontalLayout {
	private final Button joinButton;
	private final Button demitButton;
	private final Supplier<Boolean> isMember;

	public MembershipChangerComponent(String joinButtonText, String demitButtonText, Supplier<Boolean> isMember) {
		this.joinButton = new Button(joinButtonText);
		this.demitButton = new Button(demitButtonText);
		this.isMember = isMember;

		loadAppropriateButton();
		joinButton.addClickListener(x -> {
			joinButton.setVisible(false);
			demitButton.setVisible(true);
		});
		demitButton.addClickListener(x -> {
			joinButton.setVisible(true);
			demitButton.setVisible(false);
		});
		add(joinButton, demitButton);
	}

	public void loadAppropriateButton() {
		if(isMember.get()) {
			joinButton.setVisible(false);
			demitButton.setVisible(true);
		}
		else {
			demitButton.setVisible(false);
			joinButton.setVisible(true);
		}
	}

	public void addJoinButtonListener(ComponentEventListener<ClickEvent<Button>> listener){
		joinButton.addClickListener(listener);
	}

	public void addDemitButtonListener(ComponentEventListener<ClickEvent<Button>> listener){
		demitButton.addClickListener(listener);
	}

	@Override
	public void setEnabled(boolean enabled) {
		joinButton.setEnabled(enabled);
		demitButton.setEnabled(enabled);
	}
}
