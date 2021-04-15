/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import io.imunity.furms.ui.FurmsSelectFactory;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.views.user_settings.projects.ProjectsView;
import io.imunity.furms.ui.views.user_settings.ssh_keys.SSHKeysView;

import java.util.List;

public class UserSettingsMenu extends FurmsAppLayout implements AfterNavigationObserver {
	private final FurmsLayout furmsLayout;

	UserSettingsMenu(FurmsSelectFactory furmsSelectFactory) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				MenuComponent.builder(ProfileView.class).build(),
				MenuComponent.builder(SitesView.class).build(),
				MenuComponent.builder(ProjectsView.class).build(),
				MenuComponent.builder(PolicyDocumentsView.class).build(),
				MenuComponent.builder(SSHKeysView.class).build(),
				MenuComponent.builder(APIKeyView.class).build()
			),
			furmsSelectFactory
		);
		addToNavbar(false, furmsLayout.createNavbar());
		addToDrawer(furmsLayout.createDrawerContent());
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		furmsLayout.afterNavigation(getContent());
	}
}
