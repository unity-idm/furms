/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import com.vaadin.flow.component.UI;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;

public class ResourceGetter {
	public static String getCurrentResourceId(){
		return UI.getCurrent().getSession().getAttribute(FurmsViewUserContext.class).id;
	}
}
