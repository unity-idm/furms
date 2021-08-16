/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.support;

import io.imunity.furms.ui.FurmsLayoutFactory;
import io.imunity.furms.ui.components.FurmsAppLayout;
import io.imunity.furms.ui.components.FurmsLayout;
import io.imunity.furms.ui.components.MenuComponent;

import java.util.List;

public class SiteSupportMenu extends FurmsAppLayout {
	private final FurmsLayout furmsLayout;

	SiteSupportMenu(FurmsLayoutFactory furmsLayoutFactory) {
		setPrimarySection(Section.DRAWER);
		furmsLayout = furmsLayoutFactory.create(
			List.of(
				MenuComponent.builder(SignedPoliciesView.class).build()
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
