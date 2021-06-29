/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.policy_documents;

import com.vaadin.flow.router.Route;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.util.Optional;

@Route(value = "site/admin/policy/documents/acceptance", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.policy-documents.page.title")
public class PolicyDocumentAcceptanceView extends FurmsViewComponent {
	private final PolicyDocumentService policyDocumentService;

	private BreadCrumbParameter breadCrumbParameter;

	PolicyDocumentAcceptanceView(PolicyDocumentService policyDocumentService) {
		this.policyDocumentService = policyDocumentService;
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

}
