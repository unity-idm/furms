/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.fenix_admin.menu;

import java.util.List;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;

import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.views.components.FurmsAppLayout;
import io.imunity.furms.ui.views.components.FurmsLayout;
import io.imunity.furms.ui.views.fenix_admin.communites.CommunitiesView;
import io.imunity.furms.ui.views.fenix_admin.logs.AuditLogView;
import io.imunity.furms.ui.views.fenix_admin.privileges.PrivilegedUsersView;
import io.imunity.furms.ui.views.fenix_admin.sites.SitesView;

public class FenixAdminMenu extends FurmsAppLayout implements AfterNavigationObserver {
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
	public void afterNavigation(AfterNavigationEvent event) {
		furmsLayout.afterNavigation(getContent());
	}
}
