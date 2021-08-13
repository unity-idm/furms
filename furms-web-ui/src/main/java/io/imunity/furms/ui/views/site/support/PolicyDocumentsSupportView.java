/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.support;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.site.policy_documents.PolicyDocumentsView;

import static io.imunity.furms.domain.constant.RoutesConst.SITE_SUPPORT_LANDING_PAGE;

@Route(value = SITE_SUPPORT_LANDING_PAGE, layout = SiteSupportMenu.class)
@PageTitle(key = "view.site-admin.policy-documents.page.title")
class PolicyDocumentsSupportView extends PolicyDocumentsView {

	PolicyDocumentsSupportView(PolicyDocumentService policyDocumentService) {
		super(policyDocumentService, PolicyDocumentAcceptanceSupportView.class);
	}
}
