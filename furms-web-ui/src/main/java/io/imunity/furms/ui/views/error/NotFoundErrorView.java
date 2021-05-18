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
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteNotFoundError;

import io.imunity.furms.ui.components.PageTitle;

@Route(value = "/error/not-found")
@PageTitle(key = "view.error-page.not-found-error.title")
public class NotFoundErrorView extends RouteNotFoundError {

	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public NotFoundErrorView() {
		getElement().appendChild(generate(
				getTranslation("view.error-page.not-found-error.title"),
				getTranslation("view.error-page.not-found-error.message")));
	}

	@Override
	public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
		LOG.warn("Page not found: ", parameter.getException());
		return HttpServletResponse.SC_NOT_FOUND;
	}
}
