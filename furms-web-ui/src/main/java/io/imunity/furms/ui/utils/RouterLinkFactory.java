/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.ui.components.FurmsViewComponent;

import java.util.Map;

public class RouterLinkFactory {
	public static RouterLink createRouterIcon(VaadinIcon iconType, String id,
	                                          Class<? extends FurmsViewComponent> route,
	                                          String paramName, String paramValue) {
		Icon icon = iconType.create();
		return createRouterPool(icon, id, route, paramName, paramValue);
	}

	public static RouterLink createRouterPool(Component component, String id,
	                                          Class<? extends FurmsViewComponent> route,
	                                          String paramName, String paramValue) {
		RouterLink routerLink = new RouterLink("", route, id);
		routerLink.setQueryParameters(QueryParameters.simple(Map.of(paramName, paramValue)));
		routerLink.add(component);
		routerLink.setClassName("furms-color");
		return routerLink;
	}
}
