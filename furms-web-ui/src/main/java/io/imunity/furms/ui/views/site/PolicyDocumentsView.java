/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsLandingViewComponent;
import io.imunity.furms.ui.components.PageTitle;

import static io.imunity.furms.domain.constant.RoutesConst.SITE_BASE_LANDING_PAGE;

@Route(value = SITE_BASE_LANDING_PAGE, layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.policy-documents.page.title")
public class PolicyDocumentsView extends FurmsLandingViewComponent {

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		//reload page here
	}
}
