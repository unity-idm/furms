/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.administrators;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsUserComboBox;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;

@CssImport("./styles/components/furms-combo-box.css")
class SiteRoleInviteUserComponent extends HorizontalLayout {
	private final Button inviteButton;
	private final ComboBox<SiteRole> siteRoleComboBox = new ComboBox<>();
	private final FurmsUserComboBox furmsUserComboBox;

	private final Supplier<List<FURMSUser>> fetchAllUsersAction;
	private final Supplier<List<FURMSUser>> fetchCurrentUsersAction;

	SiteRoleInviteUserComponent(Supplier<List<FURMSUser>> fetchAllUsersAction, Supplier<List<FURMSUser>> fetchCurrentUsersAction) {
		this.fetchAllUsersAction = fetchAllUsersAction;
		this.fetchCurrentUsersAction = fetchCurrentUsersAction;

		siteRoleComboBox.setItems(SiteRole.values());
		siteRoleComboBox.setItemLabelGenerator(role -> getTranslation("enum.SiteRole." + role.name()));
		siteRoleComboBox.setClassName("furms-role-type-combo-box");
		siteRoleComboBox.setRequired(true);
		siteRoleComboBox.setAllowCustomValue(false);
		siteRoleComboBox.setPreventInvalidInput(true);
		siteRoleComboBox.setValue(SiteRole.ADMIN);
		siteRoleComboBox.addValueChangeListener(event -> {
			if(event.getValue() == null)
				siteRoleComboBox.setValue(event.getOldValue());
		});

		setJustifyContentMode(FlexComponent.JustifyContentMode.END);
		setSpacing(true);
		furmsUserComboBox = new FurmsUserComboBox(getAvailableUsers());
		furmsUserComboBox.setClassName("furms-invite-combo-box");
		inviteButton = new Button(getTranslation("component.invite.button"), PAPERPLANE.create());
		inviteButton.setMinWidth("auto");
		inviteButton.setEnabled(furmsUserComboBox.hasValue());
		furmsUserComboBox.addValueChangeListener(event ->
			inviteButton.setEnabled(furmsUserComboBox.hasValue() && siteRoleComboBox.getValue() != null));
		siteRoleComboBox.addValueChangeListener(event ->
			inviteButton.setEnabled(furmsUserComboBox.hasValue() && siteRoleComboBox.getValue() != null));
		add(furmsUserComboBox, siteRoleComboBox, inviteButton);
	}

	void addInviteAction(Map<SiteRole, Consumer<PersistentId>> map, Runnable gridReloader) {
		inviteButton.addClickListener((event) -> {
			Consumer<PersistentId> runnable = map.get(siteRoleComboBox.getValue());
			runnable.accept(getUserId());
			reload();
			gridReloader.run();
		});
	}

	private PersistentId getUserId() {
		return furmsUserComboBox.getValue().id.orElse(null);
	}

	void reload() {
		furmsUserComboBox.clear();
		List<FurmsViewUserModel> availableUsers = getAvailableUsers();
		furmsUserComboBox.setItems(availableUsers);
	}

	private List<FurmsViewUserModel> getAvailableUsers() {
		List<FURMSUser> users = fetchAllUsersAction.get();
		users.removeAll(fetchCurrentUsersAction.get());
		return FurmsViewUserModelMapper.mapList(users);
	}
}
