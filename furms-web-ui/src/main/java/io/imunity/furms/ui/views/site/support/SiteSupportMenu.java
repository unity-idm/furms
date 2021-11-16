/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.support;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.config.FurmsLayoutExtraPanelsConfig;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.List;

public class SiteSupportMenu extends FurmsAppLayout {

	SiteSupportMenu(FurmsLayoutFactory furmsLayoutFactory,
	                VaadinBroadcaster vaadinBroadcaster,
	                AuthzService authzService,
	                RoleTranslator roleTranslator,
	                FurmsLayoutExtraPanelsConfig extraPanelsConfig) {
		super(roleTranslator, vaadinBroadcaster, authzService, ViewMode.SITE, furmsLayoutFactory, extraPanelsConfig,
				List.of(
					MenuComponent.builder(PolicyDocumentsSupportView.class).build()));
	}
}
