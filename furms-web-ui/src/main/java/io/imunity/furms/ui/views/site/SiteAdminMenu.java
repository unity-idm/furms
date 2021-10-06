/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import com.vaadin.flow.component.applayout.AppLayout;
import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.site.administrators.SiteAdministratorsView;
import io.imunity.furms.ui.views.site.policy_documents.PolicyDocumentsView;
import io.imunity.furms.ui.views.site.resource_credits.ResourceCreditsView;
import io.imunity.furms.ui.views.site.resource_types.ResourceTypesView;
import io.imunity.furms.ui.views.site.services.InfraServicesView;
import io.imunity.furms.ui.views.site.settings.SettingsView;

import java.util.List;

public class SiteAdminMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	SiteAdminMenu(FurmsLayoutFactory furmsLayoutFactory, VaadinBroadcaster vaadinBroadcaster, AuthzService authzService, RoleTranslator roleTranslator) {
		super(roleTranslator, vaadinBroadcaster, authzService, ViewMode.SITE);

		setPrimarySection(AppLayout.Section.DRAWER);
		furmsLayout = furmsLayoutFactory.create(
			List.of(
				MenuComponent.builder(PolicyDocumentsView.class).build(),
				MenuComponent.builder(InfraServicesView.class).build(),
				MenuComponent.builder(ResourceTypesView.class).build(),
				MenuComponent.builder(ResourceCreditsView.class).build(),
				MenuComponent.builder(PendingRequestsView.class).build(),
				MenuComponent.builder(SiteAdministratorsView.class).build(),
				MenuComponent.builder(SettingsView.class).build()
			)
		);
		addToNavbar(false, furmsLayout.createNavbar());
		addToDrawer(furmsLayout.createDrawerContent());
	}

	@Override
	protected void afterNavigation() {
		super.afterNavigation();
		furmsLayout.afterNavigation(getContent());
	}
}
