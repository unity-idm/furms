/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.*;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

import java.util.List;
import java.util.Map;

import static io.imunity.furms.domain.constant.RoutesConst.*;


@Route("public/login")
@PageTitle(key = "view.login-page.title")
public class LoginView extends FurmsViewComponent {
	LoginView() {
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		Location location = event.getLocation();
		QueryParameters queryParameters = location.getQueryParameters();
		Map<String, List<String>> parametersMap = queryParameters.getParameters();
		String uri = (parametersMap.containsKey(PROXY_AUTH_PARAM) ? AUTH_REQ_BASE_URL : AUTH_REQ_PARAM_URL)
			+ REGISTRATION_ID;
		UI.getCurrent().getPage().setLocation(uri);
	}
}