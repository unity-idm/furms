/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.user_settings.invitations.InvitationsView;
import io.imunity.furms.ui.views.user_settings.policy_documents.PolicyDocumentsView;
import io.imunity.furms.ui.views.user_settings.projects.ProjectsView;
import io.imunity.furms.ui.views.user_settings.sites.SitesView;
import io.imunity.furms.ui.views.user_settings.ssh_keys.SSHKeysView;

import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class UserSettingsMenu extends FurmsAppLayout {

	UserSettingsMenu(FurmsLayoutFactory furmsLayoutFactory, VaadinBroadcaster vaadinBroadcaster, AuthzService authzService, RoleTranslator roleTranslator) {
		super(roleTranslator, vaadinBroadcaster, authzService, ViewMode.USER, furmsLayoutFactory, Stream.of(
			MenuComponent.builder(ProfileView.class).build(),
			MenuComponent.builder(InvitationsView.class).build(),
			MenuComponent.builder(SitesView.class).build(),
			MenuComponent.builder(ProjectsView.class).build(),
			MenuComponent.builder(PolicyDocumentsView.class).build(),
			MenuComponent.builder(SSHKeysView.class).build(),
			createApiKeyManagementElement(authzService))
			.filter(Objects::nonNull)
			.collect(toList())
		);
	}

	private static MenuComponent createApiKeyManagementElement(AuthzService authzService) {
		return authzService.hasRESTAPITokensCreationRights()
				? MenuComponent.builder(APIKeyView.class).build()
				: null;
	}
}
