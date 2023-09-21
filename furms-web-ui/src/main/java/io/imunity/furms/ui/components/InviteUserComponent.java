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
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;

@CssImport("./styles/components/furms-combo-box.css")
public class InviteUserComponent extends HorizontalLayout {

	private final Button inviteButton;
	private final EmailValidator emailValidator = new EmailValidator("Not valid email");
	private final FurmsUserComboBox furmsUserComboBox;

	private final Supplier<List<FURMSUser>> fetchAllUsersAction;
	private final Supplier<List<FURMSUser>> fetchCurrentUsersAction;

	public InviteUserComponent(Supplier<List<FURMSUser>> fetchAllUsersAction, Supplier<List<FURMSUser>> fetchCurrentUsersAction) {
		this.fetchAllUsersAction = fetchAllUsersAction;
		this.fetchCurrentUsersAction = fetchCurrentUsersAction;

		setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		setSpacing(true);
		furmsUserComboBox = new FurmsUserComboBox(getAvailableUsers(), true);
		furmsUserComboBox.setClassName("furms-invite-combo-box");
		inviteButton = new Button(getTranslation("component.invite.button"), PAPERPLANE.create());
		inviteButton.setMinWidth("auto");
		inviteButton.setEnabled(furmsUserComboBox.hasValue());
		inviteButton.addFocusListener(x -> {
			if(furmsUserComboBox.getEmail().isEmpty())
				inviteButton.setEnabled(false);
		});
		furmsUserComboBox.addValueChangeListener(event ->
			inviteButton.setEnabled(furmsUserComboBox.hasValue())
		);
		furmsUserComboBox.addCustomValueSetListener(customValue ->
			inviteButton.setEnabled(!emailValidator.apply(customValue, new ValueContext()).isError())
		);
		add(furmsUserComboBox, inviteButton);
	}

	public void addInviteAction(ComponentEventListener<ClickEvent<Button>> inviteAction) {
		inviteButton.addClickListener(inviteAction);
	}

	public Optional<PersistentId> getUserId() {
		return Optional.ofNullable(furmsUserComboBox.getValue())
			.flatMap(model -> model.id);
	}

	public String getEmail() {
		return furmsUserComboBox.getEmail();
	}

	public void reload() {
		furmsUserComboBox.clear();
		furmsUserComboBox.clearCustomValue(inviteButton);
		List<FurmsViewUserModel> availableUsers = getAvailableUsers();
		furmsUserComboBox.setItems(availableUsers);
	}

	public List<FurmsViewUserModel> getAvailableUsers() {
		List<FURMSUser> users = new ArrayList<>(fetchAllUsersAction.get());
		List<String> currentEmails = fetchCurrentUsersAction.get().stream()
			.map(user -> user.email)
			.toList();
		return FurmsViewUserModelMapper.mapList(
			users.stream()
				.filter(user -> !currentEmails.contains(user.email))
				.collect(Collectors.toList())
		);
	}
}
