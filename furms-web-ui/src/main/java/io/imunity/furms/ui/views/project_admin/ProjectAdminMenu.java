/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project_admin;

import java.util.List;

import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.views.components.FurmsAppLayout;
import io.imunity.furms.ui.views.components.FurmsLayout;

public class ProjectAdminMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	ProjectAdminMenu(RoleTranslator roleTranslator) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				UsersView.class,
				ResourceAccessView.class,
				ResourceAllocationsView.class,
				AlarmsView.class,
				ProjectAdministratorsView.class,
				SettingsView.class
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
