/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.components.layout.FurmsAppLayoutComponentsFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.layout.FurmsAppLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.components.layout.UserViewContextHandler;
import io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.site.administrators.SiteAdministratorsView;
import io.imunity.furms.ui.views.site.connection.PendingRequestsView;
import io.imunity.furms.ui.views.site.policy_documents.PolicyDocumentsView;
import io.imunity.furms.ui.views.site.resource_credits.ResourceCreditsView;
import io.imunity.furms.ui.views.site.resource_types.ResourceTypesView;
import io.imunity.furms.ui.views.site.services.InfraServicesView;
import io.imunity.furms.ui.views.site.settings.SettingsView;

import java.util.List;

public class SiteAdminMenu extends FurmsAppLayout {

	SiteAdminMenu(UserViewContextHandler userViewContextHandler,
	              VaadinBroadcaster vaadinBroadcaster,
	              AuthzService authzService,
	              FurmsAppLayoutComponentsFactory componentsFactory,
	              FurmsLayoutExtraPanelsConfig extraPanelsConfig) {
		super(userViewContextHandler, vaadinBroadcaster, authzService, componentsFactory, extraPanelsConfig,
				ViewMode.SITE,
				List.of(
						MenuComponent.builder(PolicyDocumentsView.class).build(),
						MenuComponent.builder(InfraServicesView.class).build(),
						MenuComponent.builder(ResourceTypesView.class).build(),
						MenuComponent.builder(ResourceCreditsView.class).build(),
						MenuComponent.builder(PendingRequestsView.class).build(),
						MenuComponent.builder(SiteAdministratorsView.class).build(),
						MenuComponent.builder(SettingsView.class).build()));
	}
}
