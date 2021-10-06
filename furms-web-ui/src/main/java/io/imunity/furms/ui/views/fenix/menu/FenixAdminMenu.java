/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.fenix.menu;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.fenix.administrators.FenixAdministratorsView;
import io.imunity.furms.ui.views.fenix.communites.CommunitiesView;
import io.imunity.furms.ui.views.fenix.communites.CommunityView;
import io.imunity.furms.ui.views.fenix.dashboard.DashboardView;
import io.imunity.furms.ui.views.fenix.logs.AuditLogView;
import io.imunity.furms.ui.views.fenix.sites.SitesView;

import java.util.List;

public class FenixAdminMenu extends FurmsAppLayout {
	FenixAdminMenu(FurmsLayoutFactory furmsLayoutFactory, VaadinBroadcaster vaadinBroadcaster, AuthzService authzService, RoleTranslator roleTranslator) {
		super(roleTranslator, vaadinBroadcaster, authzService, ViewMode.FENIX, furmsLayoutFactory,
			List.of(
				MenuComponent.builder(DashboardView.class).build(),
				MenuComponent.builder(SitesView.class).build(),
				MenuComponent.builder(CommunitiesView.class).subViews(CommunityView.class).build(),
				MenuComponent.builder(AuditLogView.class).build(),
				MenuComponent.builder(FenixAdministratorsView.class).build()
			)
		);
	}
}
