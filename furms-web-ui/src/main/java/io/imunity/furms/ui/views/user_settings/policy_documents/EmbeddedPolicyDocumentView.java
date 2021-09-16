/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.policy_documents;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

@Route(value = "users/settings/policy/documents/embedded")
@PageTitle(key = "view.user-settings.policy-documents-embedded.page.title")
class EmbeddedPolicyDocumentView extends FurmsViewComponent {

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		getContent().removeAll();
		String html = (String) UI.getCurrent().getSession().getAttribute(parameter);
		getContent().add(new Html(html));
	}
}
