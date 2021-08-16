/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.views.user_settings.policy_documents.PolicyDocumentsView;
import io.imunity.furms.ui.views.user_settings.projects.ProjectsView;
import io.imunity.furms.ui.views.user_settings.sites.SitesView;
import io.imunity.furms.ui.views.user_settings.ssh_keys.SSHKeysView;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class UserSettingsMenu extends FurmsAppLayout implements AfterNavigationObserver {
	private final FurmsLayout furmsLayout;

	UserSettingsMenu(FurmsLayoutFactory furmsLayoutFactory, AuthzService authzService) {
		setPrimarySection(Section.DRAWER);
		final List<MenuComponent> menuComponents = Stream.of(
					MenuComponent.builder(ProfileView.class).build(),
					MenuComponent.builder(SitesView.class).build(),
					MenuComponent.builder(ProjectsView.class).build(),
					MenuComponent.builder(PolicyDocumentsView.class).build(),
					MenuComponent.builder(SSHKeysView.class).build(),
					createApiKeyManagementElement(authzService))
				.filter(Objects::nonNull)
				.collect(toList());
		this.furmsLayout = furmsLayoutFactory.create(menuComponents);
		addToNavbar(false, this.furmsLayout.createNavbar());
		addToDrawer(this.furmsLayout.createDrawerContent());
	}

	private MenuComponent createApiKeyManagementElement(AuthzService authzService) {
		return authzService.hasRESTAPITokensCreationRights()
				? MenuComponent.builder(APIKeyView.class).build()
				: null;
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		furmsLayout.afterNavigation(getContent());
	}
}
