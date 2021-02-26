/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.fenix.menu;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import io.imunity.furms.ui.FurmsSelectFactory;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.views.fenix.administrators.FenixAdministratorsView;
import io.imunity.furms.ui.views.fenix.communites.CommunitiesView;
import io.imunity.furms.ui.views.fenix.communites.CommunityView;
import io.imunity.furms.ui.views.fenix.dashboard.DashboardView;
import io.imunity.furms.ui.views.fenix.logs.AuditLogView;
import io.imunity.furms.ui.views.fenix.sites.SitesView;

import java.util.List;

public class FenixAdminMenu extends FurmsAppLayout implements AfterNavigationObserver {
	private final FurmsLayout furmsLayout;

	FenixAdminMenu(FurmsSelectFactory furmsSelectFactory) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				MenuComponent.builder(DashboardView.class).build(),
				MenuComponent.builder(SitesView.class).build(),
				MenuComponent.builder(CommunitiesView.class).subViews(CommunityView.class).build(),
				MenuComponent.builder(AuditLogView.class).build(),
				MenuComponent.builder(FenixAdministratorsView.class).build()
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
