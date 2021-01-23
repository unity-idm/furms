/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import java.util.List;

import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;
import io.imunity.furms.ui.user_context.RoleTranslator;

public class SiteSupportMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	SiteSupportMenu(RoleTranslator roleTranslator) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				MenuComponent.builder(SignedPoliciesView.class).build()
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
