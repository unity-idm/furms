/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.fenix.menu;

import java.util.List;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;

import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.views.fenix.administrators.FenixAdministratorsView;
import io.imunity.furms.ui.views.fenix.communites.CommunitiesView;
import io.imunity.furms.ui.views.fenix.dashboard.DashboardView;
import io.imunity.furms.ui.views.fenix.logs.AuditLogView;
import io.imunity.furms.ui.views.fenix.sites.SitesView;

public class FenixAdminMenu extends FurmsAppLayout implements AfterNavigationObserver {
	private final FurmsLayout furmsLayout;

	FenixAdminMenu(RoleTranslator roleTranslator) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				DashboardView.class,
				SitesView.class,
				CommunitiesView.class,
				AuditLogView.class,
				FenixAdministratorsView.class
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
