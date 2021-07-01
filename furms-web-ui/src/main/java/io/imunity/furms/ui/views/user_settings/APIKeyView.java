/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.user.api.key.UserApiKeyService;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.domain.users.key.UserApiKey;
import io.imunity.furms.ui.components.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;

@Route(value = "users/settings/api/keys", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.api-key.page.title")
public class APIKeyView extends FurmsViewComponent {

	private final UserApiKeyService userApiKeyService;
	private final AuthzService authzService;

	private final CopyToClipboardStringComponent apiKeyFormItem;

	private final PersistentId userId;

	public APIKeyView(UserApiKeyService userApiKeyService, AuthzService authzService) {
		this.userApiKeyService = userApiKeyService;
		this.authzService = authzService;
		this.userId = this.authzService.getCurrentAuthNUser().id
				.orElseThrow(() -> new AccessDeniedException("User ID not found in security context"));

		final String apiKey = userApiKeyService.findByUserId(userId)
				.map(UserApiKey::getApiKey)
				.map(UUID::toString)
				.orElse(null);

		final FurmsFormLayout furmsFormLayout = new FurmsFormLayout();

		furmsFormLayout.addFormItem(
				new CopyToClipboardStringComponent(userId.id,
						getTranslation("view.user-settings.api-key.form.name.copy.msg")),
				getTranslation("view.user-settings.api-key.form.name.label"));

		this.apiKeyFormItem = new CopyToClipboardStringComponent("",
				getTranslation("view.user-settings.api-key.form.key.copy.msg"));
		setApiKeyFormItemValue(apiKey);
		furmsFormLayout.addFormItem(
				apiKeyFormItem,
				getTranslation("view.user-settings.api-key.form.key.label"));

		final Button generate = new Button(getTranslation("view.user-settings.api-key.form.button.generate"),
				e -> generateAPIKeyAction());
		generate.setEnabled(isApiKeyFormItemEmpty());
		this.apiKeyFormItem.addValueChangeListener(event -> generate.setEnabled(isApiKeyFormItemEmpty()));
		final Button revoke = new Button(getTranslation("view.user-settings.api-key.form.button.revoke"),
				e -> revokeAPIKeyAction());
		final FormButtons formButtons = new FormButtons(generate, revoke);

		getContent().add(furmsFormLayout, formButtons);
	}

	private void generateAPIKeyAction() {
		setApiKeyFormItemValue(generateAPIKey());
	}

	private String generateAPIKey() {
		return userApiKeyService.generate(userId)
				.or(() -> {
					showErrorNotification(getTranslation("view.user-settings.api-key.form.button.generate.error"));
					return Optional.empty();
				})
				.map(UserApiKey::getApiKey)
				.map(UUID::toString)
				.get();
	}

	private void revokeAPIKeyAction() {
		userApiKeyService.revoke(userId);
		setApiKeyFormItemValue(null);
	}

	private boolean isApiKeyFormItemEmpty() {
		return apiKeyFormItem.getValue().equals(getTranslation("view.user-settings.api-key.form.key.empty"));
	}

	private void setApiKeyFormItemValue(String apiKey) {
		if (StringUtils.isBlank(apiKey)) {
			apiKeyFormItem.setValue(getTranslation("view.user-settings.api-key.form.key.empty"));
			apiKeyFormItem.setReadOnly(false);
		} else {
			apiKeyFormItem.setValue(apiKey);
			apiKeyFormItem.setReadOnly(true);
		}
	}

}
