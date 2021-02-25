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
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;

import java.util.List;
import java.util.function.Supplier;

import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;

@CssImport("./styles/components/furms-combo-box.css")
public class InviteUserComponent extends HorizontalLayout {

	private final Button inviteButton;
	private final FurmsUserComboBox furmsUserComboBox;

	private final Supplier<List<FURMSUser>> fetchAllUsersAction;
	private final Supplier<List<FURMSUser>> fetchCurrentUsersAction;

	public InviteUserComponent(Supplier<List<FURMSUser>> fetchAllUsersAction, Supplier<List<FURMSUser>> fetchCurrentUsersAction) {
		this.fetchAllUsersAction = fetchAllUsersAction;
		this.fetchCurrentUsersAction = fetchCurrentUsersAction;

		setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		setSpacing(true);
		furmsUserComboBox = new FurmsUserComboBox(getAvailableUsers());
		furmsUserComboBox.setClassName("furms-invite-combo-box");
		inviteButton = new Button(getTranslation("component.invite.button"), PAPERPLANE.create());
		inviteButton.setMinWidth("auto");
		inviteButton.setEnabled(furmsUserComboBox.hasValue());
		furmsUserComboBox.addValueChangeListener(event ->
			inviteButton.setEnabled(furmsUserComboBox.hasValue()));
		add(furmsUserComboBox, inviteButton);
	}

	public void addInviteAction(ComponentEventListener<ClickEvent<Button>> inviteAction) {
		inviteButton.addClickListener(inviteAction);
	}

	public String getUserId() {
		return furmsUserComboBox.getValue().id;
	}

	public void reload() {
		furmsUserComboBox.clear();
		List<FurmsViewUserModel> availableUsers = getAvailableUsers();
		furmsUserComboBox.setItems(availableUsers);
	}

	public List<FurmsViewUserModel> getAvailableUsers() {
		List<FURMSUser> users = fetchAllUsersAction.get();
		users.removeAll(fetchCurrentUsersAction.get());
		return FurmsViewUserModelMapper.mapList(users);
	}
}
