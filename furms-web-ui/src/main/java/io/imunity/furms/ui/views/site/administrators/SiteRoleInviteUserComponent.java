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
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import io.imunity.furms.api.validation.exceptions.UserIsSiteAdmin;
import io.imunity.furms.api.validation.exceptions.UserIsSiteSupport;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FurmsUserComboBox;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;
import io.imunity.furms.ui.utils.CommonExceptionsHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.icon.VaadinIcon.PAPERPLANE;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;

@CssImport("./styles/components/furms-combo-box.css")
class SiteRoleInviteUserComponent extends HorizontalLayout {
	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Button inviteButton;
	private final ComboBox<SiteRole> siteRoleComboBox = new ComboBox<>();
	private final FurmsUserComboBox furmsUserComboBox;
	private final EmailValidator emailValidator = new EmailValidator("Not valid email");

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
		siteRoleComboBox.setValue(SiteRole.ADMIN);
		siteRoleComboBox.addValueChangeListener(event -> {
			if(event.getValue() == null)
				siteRoleComboBox.setValue(event.getOldValue());
		});

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
			inviteButton.setEnabled(furmsUserComboBox.hasValue() && siteRoleComboBox.getValue() != null));
		siteRoleComboBox.addValueChangeListener(event ->
			inviteButton.setEnabled((isMailValid(furmsUserComboBox.getEmail()) || furmsUserComboBox.hasValue()) && siteRoleComboBox.getValue() != null));
		furmsUserComboBox.addCustomValueSetListener(customValue ->
			inviteButton.setEnabled(isMailValid(customValue))
		);
		add(furmsUserComboBox, siteRoleComboBox, inviteButton);
	}

	private boolean isMailValid(String email) {
		return !emailValidator.apply(email, new ValueContext()).isError();
	}

	void addInviteAction(Map<SiteRole, Consumer<PersistentId>> existingUserInvitators,
	                     Map<SiteRole, Consumer<String>> newUserInvitators,
	                     Runnable gridReloader) {
		inviteButton.addClickListener((event) -> {
			try {
				getUserId().ifPresentOrElse(
					id -> existingUserInvitators.get(siteRoleComboBox.getValue()).accept(id),
					() -> newUserInvitators.get(siteRoleComboBox.getValue()).accept(furmsUserComboBox.getEmail())
				);
				reload();
				gridReloader.run();
				showSuccessNotification(getTranslation("invite.successful.added"));
			} catch (UserIsSiteAdmin e) {
				showErrorNotification(getTranslation("invite.error.role.site.admin"));
			} catch (UserIsSiteSupport e) {
				showErrorNotification(getTranslation("invite.error.role.site.support"));
			} catch (RuntimeException e) {
				boolean handled = CommonExceptionsHandler.showExceptionBasedNotificationError(e);
				if(!handled)
					LOG.error("Could not invite site user.");
			}
		});
	}

	private Optional<PersistentId> getUserId() {
		return Optional.ofNullable(furmsUserComboBox.getValue()).flatMap(userModel -> userModel.id);
	}

	void reload() {
		furmsUserComboBox.clear();
		furmsUserComboBox.clearCustomValue(inviteButton);
		List<FurmsViewUserModel> availableUsers = getAvailableUsers();
		furmsUserComboBox.setItems(availableUsers);
	}

	private List<FurmsViewUserModel> getAvailableUsers() {
		List<FURMSUser> users = fetchAllUsersAction.get();
		List<String> currentEmails = fetchCurrentUsersAction.get().stream()
			.map(user -> user.email)
			.collect(Collectors.toList());

		return FurmsViewUserModelMapper.mapList(
			users.stream()
				.filter(user -> !currentEmails.contains(user.email))
				.collect(Collectors.toList())
		);
	}
}
