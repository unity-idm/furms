/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import java.util.List;

import com.vaadin.flow.component.applayout.AppLayout;

import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;

public class SiteAdminMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	SiteAdminMenu(RoleTranslator roleTranslator) {
		setPrimarySection(AppLayout.Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				PolicyDocumentsView.class,
				ServicesView.class,
				ResourceTypesView.class,
				ResourceCreditsView.class,
				PendingRequestsView.class,
				SiteAdministratorsView.class,
				SettingsView.class
			),
			roleTranslator
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
