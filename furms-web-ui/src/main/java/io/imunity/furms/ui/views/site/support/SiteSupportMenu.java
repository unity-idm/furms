/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.support;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.components.layout.FurmsAppLayoutComponentsFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.layout.FurmsAppLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.components.layout.UserViewContextHandler;
import io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.List;

public class SiteSupportMenu extends FurmsAppLayout {

	SiteSupportMenu(UserViewContextHandler userViewContextHandler,
	                VaadinBroadcaster vaadinBroadcaster,
	                AuthzService authzService,
	                FurmsAppLayoutComponentsFactory componentsFactory,
	                FurmsLayoutExtraPanelsConfig extraPanelsConfig) {
		super(userViewContextHandler, vaadinBroadcaster, authzService, componentsFactory, extraPanelsConfig,
				ViewMode.SITE,
				List.of(
					MenuComponent.builder(PolicyDocumentsSupportView.class).build()));
	}
}
