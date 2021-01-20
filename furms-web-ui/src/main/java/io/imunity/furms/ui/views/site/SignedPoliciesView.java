/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site;

import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

import static io.imunity.furms.domain.constant.RoutesConst.SITE_SUPPORT_LANDING_PAGE;

@Route(value = SITE_SUPPORT_LANDING_PAGE, layout = SiteSupportMenu.class)
@PageTitle(key = "view.site-support.signed-policies.page.title")
public class SignedPoliciesView extends FurmsViewComponent {
}
