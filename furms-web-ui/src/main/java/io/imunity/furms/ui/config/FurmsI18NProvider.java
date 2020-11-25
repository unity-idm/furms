/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.config;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toMap;

@Component
public class FurmsI18NProvider implements I18NProvider
{
	private final Map<String, ResourceBundle> translationMap;

	private FurmsI18NProvider()
	{
		translationMap = getProvidedLocales().stream()
			.collect(toMap(Locale::getLanguage, l -> ResourceBundle.getBundle("messages", l)));
	}

	@Override
	public List<Locale> getProvidedLocales()
	{
		return unmodifiableList(singletonList(new Locale("en")));
	}

	@Override
	public String getTranslation(String s, Locale locale, Object... objects)
	{
		return translationMap.get(locale.getLanguage()).getString(s);
	}
}
