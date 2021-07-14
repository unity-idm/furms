/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.error;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.components.PageTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandles;

import static io.imunity.furms.ui.views.error.DefaultErrorViewsGenerator.generate;

@Tag("div")
@Route(value = "/error/access-denied")
@PageTitle(key = "view.error-page.access-denied-error.title")
public class AccessDeniedErrorView extends Component implements HasErrorParameter<AccessDeniedException> {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public AccessDeniedErrorView() {
		getElement().appendChild(generate(
				getTranslation("view.error-page.access-denied-error.title"),
				getTranslation("view.error-page.access-denied-error.message")));
	}

	@Override
	public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<AccessDeniedException> parameter) {
		LOG.warn("Access Denied: ", parameter.getException());
		return HttpServletResponse.SC_FORBIDDEN;
	}
}
