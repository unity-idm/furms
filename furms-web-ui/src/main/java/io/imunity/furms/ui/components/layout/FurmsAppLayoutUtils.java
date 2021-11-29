/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.layout;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

public class FurmsAppLayoutUtils {

	public static String getPageTitle(Class<? extends Component> componentClass) {
		String key = componentClass.getAnnotation(PageTitle.class).key();
		return getTranslation(key);
	}

	public static void callReloadLogo(final Class<? extends FurmsViewComponent> source) {
		UI.getCurrent().navigate(source);
	}
}
