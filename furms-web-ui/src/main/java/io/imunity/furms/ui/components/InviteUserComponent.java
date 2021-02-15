/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.imunity.furms.domain.users.User;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;

import java.util.List;
import java.util.function.Supplier;

import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;

@CssImport("./styles/components/furms-combo-box.css")
public class InviteUserComponent extends HorizontalLayout {

	private final Button inviteButton;
	private final FurmsUserComboBox furmsUserComboBox;

	private final Supplier<List<User>> fetchAllUsersAction;
	private final Supplier<List<User>> fetchCurrentUsersAction;

	public InviteUserComponent(Supplier<List<User>> fetchAllUsersAction, Supplier<List<User>> fetchCurrentUsersAction) {
		this.fetchAllUsersAction = fetchAllUsersAction;
		this.fetchCurrentUsersAction = fetchCurrentUsersAction;

		setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		setSpacing(true);
		furmsUserComboBox = new FurmsUserComboBox(getAvailableUsers());
		furmsUserComboBox.setAlignItems(Alignment.END);
		furmsUserComboBox.comboBox.setClassName("furms-invite-combo-box");
		inviteButton = new Button(getTranslation("component.invite.button"), PAPERPLANE.create());
		inviteButton.setMinWidth("auto");
		inviteButton.setEnabled(furmsUserComboBox.hasValue());
		furmsUserComboBox.comboBox.addValueChangeListener(event -> 
			inviteButton.setEnabled(furmsUserComboBox.hasValue()));
		add(furmsUserComboBox, inviteButton);
	}

	public void addInviteAction(ComponentEventListener<ClickEvent<Button>> inviteAction) {
		inviteButton.addClickListener(inviteAction);
	}

	public String getEmail() {
		return furmsUserComboBox.comboBox.getValue().email;
	}

	public void reload() {
		furmsUserComboBox.comboBox.clear();
		List<FurmsViewUserModel> availableUsers = getAvailableUsers();
		furmsUserComboBox.comboBox.setItems(availableUsers);
	}

	public List<FurmsViewUserModel> getAvailableUsers() {
		List<User> users = fetchAllUsersAction.get();
		users.removeAll(fetchCurrentUsersAction.get());
		return FurmsViewUserModelMapper.mapList(users);
	}
}
