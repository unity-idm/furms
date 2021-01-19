/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community_admin;

import java.util.List;

import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.views.components.FurmsAppLayout;
import io.imunity.furms.ui.views.components.FurmsLayout;

public class CommunityAdminMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	CommunityAdminMenu(RoleTranslator roleTranslator) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				DashboardView.class,
				ProjectsView.class,
				GroupsView.class,
				CommunityAdminsView.class,
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
