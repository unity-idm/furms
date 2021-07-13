/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.user.api.key.UserApiKeyService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.key.UserApiKey;
import io.imunity.furms.ui.components.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import java.lang.invoke.MethodHandles;
import java.util.UUID;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;

@Route(value = "users/settings/api/keys", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.api-key.page.title")
public class APIKeyView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final UserApiKeyService userApiKeyService;
	private final AuthzService authzService;

	private final CopyToClipboardStringComponent apiKeyFormItem;
	private final Button generateRevokeButton;
	private final Button cancelButton;
	private final Button saveButton;

	private final PersistentId userId;

	public APIKeyView(UserApiKeyService userApiKeyService, AuthzService authzService) {
		this.userApiKeyService = userApiKeyService;
		this.authzService = authzService;
		this.userId = this.authzService.getCurrentAuthNUser().id
				.orElseThrow(() -> new AccessDeniedException("User ID not found in security context"));

		final String apiKey = loadApiKey();

		final FurmsFormLayout furmsFormLayout = new FurmsFormLayout();

		furmsFormLayout.addFormItem(
				new CopyToClipboardStringComponent(userId.id,
						getTranslation("view.user-settings.api-key.form.name.copy.msg")),
				getTranslation("view.user-settings.api-key.form.name.label"));

		this.apiKeyFormItem = new CopyToClipboardStringComponent("",
				getTranslation("view.user-settings.api-key.form.key.copy.msg"));
		this.apiKeyFormItem.setWidth("25em");
		this.generateRevokeButton = new Button();

		final HorizontalLayout apiKeyRow = new HorizontalLayout(this.apiKeyFormItem, generateRevokeButton);
		apiKeyRow.getStyle().set("align-items", "center");
		furmsFormLayout.addFormItem(
				apiKeyRow,
				getTranslation("view.user-settings.api-key.form.key.label"));

		this.cancelButton = new Button(
				getTranslation("view.user-settings.api-key.form.button.cancel"),
				e -> cancelAction());
		this.saveButton = new Button(
				getTranslation("view.user-settings.api-key.form.button.save"),
				e -> saveApiKeyAction());
		this.saveButton.addThemeVariants(LUMO_PRIMARY);
		setApiKeyFormItemValue(apiKey);
		this.saveButton.setEnabled(false);

		final FormButtons formButtons = new FormButtons(cancelButton, saveButton);

		getContent().add(furmsFormLayout, formButtons);
	}

	private String loadApiKey() {
		return userApiKeyService.findByUserId(userId)
				.map(UserApiKey::getApiKey)
				.map(UUID::toString)
				.orElse(null);
	}

	private void cancelAction() {
		setApiKeyFormItemValue(loadApiKey());
		saveButton.setEnabled(false);
	}

	private void generateAPIKeyAction() {
		setApiKeyFormItemValue(UUID.randomUUID().toString());
	}

	private void saveApiKeyAction() {
		try {
			if (StringUtils.isBlank(apiKeyFormItem.getValue())) {
				userApiKeyService.revoke(userId);
				showSuccessNotification(getTranslation("view.user-settings.api-key.form.save.success.revoke.message"));
			} else {
				userApiKeyService.save(userId, apiKeyFormItem.getValue());
				showSuccessNotification(getTranslation("view.user-settings.api-key.form.save.success.message"));
			}
			saveButton.setEnabled(false);
		} catch (Exception e) {
			LOG.error("Unable to save API KEY for user=" + userId, e);
			showErrorNotification(getTranslation("view.user-settings.api-key.form.button.generate.error"));
		}
	}

	private void revokeAPIKeyAction() {
		setApiKeyFormItemValue(null);
	}

	private void setApiKeyFormItemValue(String apiKey) {
		if (StringUtils.isBlank(apiKey)) {
			apiKeyFormItem.setValue("");
			apiKeyFormItem.setReadOnly(false);
			generateRevokeButton.setText(getTranslation("view.user-settings.api-key.form.button.generate"));
			generateRevokeButton.addClickListener(event -> generateAPIKeyAction());
		} else {
			apiKeyFormItem.setValue(apiKey);
			apiKeyFormItem.setReadOnly(true);
			generateRevokeButton.setText(getTranslation("view.user-settings.api-key.form.button.revoke"));
			generateRevokeButton.addClickListener(event -> revokeAPIKeyAction());
		}
		saveButton.setEnabled(true);
	}

}
