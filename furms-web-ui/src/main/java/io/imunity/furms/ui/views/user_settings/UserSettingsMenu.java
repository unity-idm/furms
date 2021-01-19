/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings;

import java.util.List;

import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.views.components.FurmsAppLayout;
import io.imunity.furms.ui.views.components.FurmsLayout;

public class UserSettingsMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	UserSettingsMenu(RoleTranslator roleTranslator) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				ProfileView.class,
				SitesView.class,
				ProjectsView.class,
				PolicyDocumentsView.class,
				SSHKeysView.class,
				APIKeyView.class
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
