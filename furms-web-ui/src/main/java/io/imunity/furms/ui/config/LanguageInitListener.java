/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Value;

import java.util.Locale;

@SpringComponent
class LanguageInitListener implements VaadinServiceInitListener
{
	@Value("${furms.language}")
	private String language;

	@Override
	public void serviceInit(ServiceInitEvent serviceInitEvent)
	{
		serviceInitEvent.getSource().addUIInitListener(uiInitEvent ->
			uiInitEvent.getUI().setLocale(new Locale(language))
		);
	}
}
