package io.imunity.furms.ui.config;

import com.vaadin.flow.i18n.I18NProvider;

public interface FurmsI18NProvider extends I18NProvider {
	String getTranslation(String key, Object... params);
}
