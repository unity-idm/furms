/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.community.adminstrators.CommunityAdminsView;
import io.imunity.furms.ui.views.community.projects.ProjectView;
import io.imunity.furms.ui.views.community.projects.ProjectsView;
import io.imunity.furms.ui.views.community.settings.SettingsView;

import java.util.List;

public class CommunityAdminMenu extends FurmsAppLayout implements AfterNavigationObserver {
	private final FurmsLayout furmsLayout;

	CommunityAdminMenu(FurmsLayoutFactory furmsLayoutFactory, RoleTranslator roleTranslator) {
		super(roleTranslator, ViewMode.COMMUNITY);
		setPrimarySection(Section.DRAWER);
		furmsLayout = furmsLayoutFactory.create(
			List.of(
				MenuComponent.builder(DashboardView.class).build(),
				MenuComponent.builder(ProjectsView.class).subViews(ProjectView.class).build(),
				MenuComponent.builder(GroupsView.class).build(),
				MenuComponent.builder(CommunityAdminsView.class).build(),
				MenuComponent.builder(SettingsView.class).build()
			)
		);
		addToNavbar(false, furmsLayout.createNavbar());
		addToDrawer(furmsLayout.createDrawerContent());
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		furmsLayout.afterNavigation(getContent());
	}
}
