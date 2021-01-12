/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site_admin;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.PreserveOnRefresh;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.views.components.FurmsLayout;

import java.util.List;

@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@PreserveOnRefresh
public class SiteAdminMenu extends AppLayout{
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
