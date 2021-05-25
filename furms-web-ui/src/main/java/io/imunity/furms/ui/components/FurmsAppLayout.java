/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import io.imunity.furms.ui.user_context.FurmsViewUserContext;

@JsModule("./styles/shared-styles.js")
@CssImport("./styles/views/main/main-view.css")
@CssImport("./styles/custom-lumo-theme.css")
@Theme(value = Lumo.class)
@PreserveOnRefresh
@Push
public class FurmsAppLayout extends AppLayout {
	
	private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private FurmsViewUserContext savedUserContext;
	
	public FurmsAppLayout() {
		savedUserContext = FurmsViewUserContext.getCurrent();
		LOG.debug("Saving UI state {}", savedUserContext);
	}
	
	@Override
	public void onAttach(AttachEvent event) {
		if (FurmsViewUserContext.getCurrent() == null) {
			LOG.info("Recreate furms user context from saved state {}", savedUserContext);
			savedUserContext.setAsCurrent();
		}
	}
}
