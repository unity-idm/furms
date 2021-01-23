/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import java.util.List;

import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.user_context.RoleTranslator;

public class UserSettingsMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	UserSettingsMenu(RoleTranslator roleTranslator) {
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
			roleTranslator
		);
		addToNavbar(false, furmsLayout.createNavbar());
		addToDrawer(furmsLayout.createDrawerContent());
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		furmsLayout.afterNavigation(getContent());
	}
}
