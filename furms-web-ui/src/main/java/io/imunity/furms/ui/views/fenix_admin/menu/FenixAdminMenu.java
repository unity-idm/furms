/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.fenix_admin.menu;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.router.PreserveOnRefresh;
import io.imunity.furms.api.authz.RoleTranslator;
import io.imunity.furms.ui.views.components.FurmsLayout;
import io.imunity.furms.ui.views.fenix_admin.communites.CommunitiesView;
import io.imunity.furms.ui.views.fenix_admin.logs.AuditLogView;
import io.imunity.furms.ui.views.fenix_admin.privileges.PrivilegedUsersView;
import io.imunity.furms.ui.views.fenix_admin.sites.SitesView;

import java.util.List;

@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@PreserveOnRefresh
public class FenixAdminMenu extends AppLayout {
	private final FurmsLayout furmsLayout;

	FenixAdminMenu(RoleTranslator roleTranslator) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				SitesView.class,
				CommunitiesView.class,
				AuditLogView.class,
				PrivilegedUsersView.class
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
