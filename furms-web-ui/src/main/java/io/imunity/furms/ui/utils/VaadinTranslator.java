/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;

public class VaadinTranslator {
	public static String getTranslation(String key) {
		return VaadinService.getCurrent()
			.getInstantiator()
			.getI18NProvider()
			.getTranslation(key, UI.getCurrent().getLocale());
	}
}
