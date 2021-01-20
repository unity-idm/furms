/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.views.community.projects.ProjectsView;

import java.util.List;

public class CommunityAdminMenu extends FurmsAppLayout implements AfterNavigationObserver {
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
	public void afterNavigation(AfterNavigationEvent event) {
		furmsLayout.afterNavigation(getContent());
	}
}
