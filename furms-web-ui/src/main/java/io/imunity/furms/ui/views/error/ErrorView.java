/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.error;

import static io.imunity.furms.ui.views.error.DefaultErrorViewsGenerator.generate;

import java.lang.invoke.MethodHandles;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.InternalServerError;
import com.vaadin.flow.router.Route;

import io.imunity.furms.ui.components.PageTitle;

@Route(value = "/error/internal-server")
@PageTitle(key = "view.error-page.internal-server-error.title")
public class ErrorView extends InternalServerError {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public ErrorView() {
		getElement().appendChild(generate(
				getTranslation("view.error-page.internal-server-error.title"),
				getTranslation("view.error-page.internal-server-error.message")));
	}

	@Override
	public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<Exception> parameter) {
		LOG.warn("Unexpected error: ", parameter.getException());
		return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
	}
}
