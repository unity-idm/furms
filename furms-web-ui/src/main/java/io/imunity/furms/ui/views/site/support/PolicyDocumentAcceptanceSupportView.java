/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.support;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.site.policy_documents.PolicyDocumentAcceptanceView;

@Route(value = "site/support/policy/documents/acceptance", layout = SiteSupportMenu.class)
@PageTitle(key = "view.site-admin.policy-documents-acceptance.page.title")
class PolicyDocumentAcceptanceSupportView extends PolicyDocumentAcceptanceView {

	PolicyDocumentAcceptanceSupportView(PolicyDocumentService policyDocumentService) {
		super(policyDocumentService);
	}
}
