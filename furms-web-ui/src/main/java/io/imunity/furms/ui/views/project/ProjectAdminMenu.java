/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import io.imunity.furms.ui.FurmsSelectFactory;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.views.project.administrators.ProjectAdministratorsView;
import io.imunity.furms.ui.views.project.allocations.ResourceAllocationsView;
import io.imunity.furms.ui.views.project.resource_access.ResourceAccessView;
import io.imunity.furms.ui.views.project.settings.SettingsView;
import io.imunity.furms.ui.views.project.sites.SitesView;
import io.imunity.furms.ui.views.project.users.UsersView;

import java.util.List;

public class ProjectAdminMenu extends FurmsAppLayout implements AfterNavigationObserver {
	private final FurmsLayout furmsLayout;

	ProjectAdminMenu(FurmsSelectFactory furmsSelectFactory) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				MenuComponent.builder(UsersView.class).build(),
				MenuComponent.builder(SitesView.class).build(),
				MenuComponent.builder(ResourceAccessView.class).build(),
				MenuComponent.builder(ResourceAllocationsView.class).build(),
				MenuComponent.builder(AlarmsView.class).build(),
				MenuComponent.builder(ProjectAdministratorsView.class).build(),
				MenuComponent.builder(SettingsView.class).build()
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
