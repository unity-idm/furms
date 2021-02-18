/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import com.vaadin.flow.component.applayout.AppLayout;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.FurmsSelect;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.views.site.administrators.SiteAdministratorsView;
import io.imunity.furms.ui.views.site.settings.SettingsView;

import java.util.List;

public class SiteAdminMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	SiteAdminMenu(FurmsSelect furmsSelect) {
		setPrimarySection(AppLayout.Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				MenuComponent.builder(PolicyDocumentsView.class).build(),
				MenuComponent.builder(ServicesView.class).build(),
				MenuComponent.builder(ResourceTypesView.class).build(),
				MenuComponent.builder(ResourceCreditsView.class).build(),
				MenuComponent.builder(PendingRequestsView.class).build(),
				MenuComponent.builder(SiteAdministratorsView.class).build(),
				MenuComponent.builder(SettingsView.class).build()
			),
			furmsSelect
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
