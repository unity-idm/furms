/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import java.util.List;

import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;

public class SiteSupportMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	SiteSupportMenu(RoleTranslator roleTranslator) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = new FurmsLayout(
			List.of(
				SignedPoliciesView.class
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
