/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.config;

import com.vaadin.flow.component.UI;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.stream.Collectors.toMap;

@Component
class FurmsI18NProviderImpl implements FurmsI18NProvider {
	private static final Locale DEFAULT_LOCALE = new Locale("en");
	private final Map<String, ResourceBundle> translationMap;

	private FurmsI18NProviderImpl() {
		translationMap = getProvidedLocales().stream()
			.collect(toMap(Locale::getLanguage, l -> ResourceBundle.getBundle("messages", l)));
	}

	@Override
	public List<Locale> getProvidedLocales() {
		return List.of(DEFAULT_LOCALE);
	}

	@Override
	public String getTranslation(String s, Locale locale, Object... objects) {
		return MessageFormat.format(translationMap.get(locale.getLanguage()).getString(s), objects);
	}
	
	@Override
	public String getTranslation(String key, Object... params) {
		return getTranslation(key, getUILocale(), params);
	}
	
	private Locale getUILocale() {
		UI currentUi = UI.getCurrent();
		Locale locale = currentUi == null ? null : currentUi.getLocale();
		if (locale == null) {
			locale = DEFAULT_LOCALE;
		}
		return locale;
	}
}
