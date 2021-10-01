/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.groups;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;
import io.imunity.furms.ui.components.FurmsUserComboBox;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;

@CssImport("./styles/components/furms-combo-box.css")
public class AddGroupComponent extends HorizontalLayout {

	private final Button inviteButton;
	private final FurmsUserComboBox furmsUserComboBox;

	private final Supplier<List<FURMSUser>> fetchAllUsersAction;
	private final Supplier<List<FURMSUser>> fetchCurrentUsersAction;

	AddGroupComponent(Supplier<List<FURMSUser>> fetchAllUsersAction, Supplier<List<FURMSUser>> fetchCurrentUsersAction) {
		this.fetchAllUsersAction = fetchAllUsersAction;
		this.fetchCurrentUsersAction = fetchCurrentUsersAction;

		setJustifyContentMode(JustifyContentMode.END);
		setSpacing(true);
		furmsUserComboBox = new FurmsUserComboBox(getAvailableUsers(), false);
		furmsUserComboBox.setClassName("furms-invite-combo-box");
		inviteButton = new Button(getTranslation("component.add-group.button"), PLUS_CIRCLE.create());
		inviteButton.setMinWidth("auto");
		inviteButton.setEnabled(furmsUserComboBox.hasValue());
		furmsUserComboBox.addValueChangeListener(event ->
			inviteButton.setEnabled(furmsUserComboBox.hasValue())
		);
		furmsUserComboBox.addValueChangeListener(event -> {
			if(event.getValue() == null)
				furmsUserComboBox.setValue(event.getOldValue());
		});
		add(furmsUserComboBox, inviteButton);
	}

	public void addAddingAction(ComponentEventListener<ClickEvent<Button>> inviteAction) {
		inviteButton.addClickListener(inviteAction);
	}

	public Optional<FenixUserId> getFenixUserId() {
		return Optional.ofNullable(furmsUserComboBox.getValue())
			.flatMap(model -> model.fenixUserId);
	}

	public void reload() {
		furmsUserComboBox.setItems(getAvailableUsers());
	}

	public List<FurmsViewUserModel> getAvailableUsers() {
		List<FURMSUser> users = fetchAllUsersAction.get();
		users.removeAll(fetchCurrentUsersAction.get());
		return FurmsViewUserModelMapper.mapList(users);
	}
}
