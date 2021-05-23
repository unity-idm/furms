/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import io.imunity.furms.ui.user_context.FurmsViewUserContext;

public class ResourceGetter {
	public static String getCurrentResourceId(){
		return FurmsViewUserContext.getCurrent().id;
	}
}
