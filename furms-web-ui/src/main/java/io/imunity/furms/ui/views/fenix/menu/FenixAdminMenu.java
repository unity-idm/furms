/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.fenix.menu;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.components.layout.FurmsAppLayout;
import io.imunity.furms.ui.components.layout.FurmsAppLayoutComponentsFactory;
import io.imunity.furms.ui.components.layout.UserViewContextHandler;
import io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.fenix.administrators.FenixAdministratorsView;
import io.imunity.furms.ui.views.fenix.communites.CommunitiesView;
import io.imunity.furms.ui.views.fenix.communites.CommunityView;
import io.imunity.furms.ui.views.fenix.communites.allocations.CommunityAllocationsDetailsView;
import io.imunity.furms.ui.views.fenix.dashboard.DashboardView;
import io.imunity.furms.ui.views.fenix.logs.AuditLogView;
import io.imunity.furms.ui.views.fenix.sites.SitesView;

import java.util.List;

public class FenixAdminMenu extends FurmsAppLayout {

	FenixAdminMenu(UserViewContextHandler userViewContextHandler,
	               VaadinBroadcaster vaadinBroadcaster,
	               AuthzService authzService,
	               FurmsAppLayoutComponentsFactory componentsFactory,
	               FurmsLayoutExtraPanelsConfig extraPanelsConfig) {
		super(userViewContextHandler, vaadinBroadcaster, authzService, componentsFactory, extraPanelsConfig,
				ViewMode.FENIX,
				List.of(
						MenuComponent.builder(DashboardView.class).build(),
						MenuComponent.builder(SitesView.class).build(),
						MenuComponent.builder(CommunitiesView.class).subViews(CommunityView.class, CommunityAllocationsDetailsView.class).build(),
						MenuComponent.builder(AuditLogView.class).build(),
						MenuComponent.builder(FenixAdministratorsView.class).build()));
	}
}
