/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.utils;

import static io.imunity.furms.ui.utils.MenuComponentFactory.createActionButton;

import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouterLink;

import io.imunity.furms.ui.components.FurmsViewComponent;

public class RouterLinkFactory {
	public static RouterLink createRouterIcon(VaadinIcon iconType, String id,
	                                          Class<? extends FurmsViewComponent> route,
	                                          String paramName, String paramValue) {
		Component icon = createActionButton(iconType);
		return createRouterPool(icon, id, route, paramName, paramValue);
	}

	public static RouterLink createRouterPool(Component component, String id,
	                                          Class<? extends FurmsViewComponent> route,
	                                          String paramName, String paramValue) {
		RouterLink routerLink = new RouterLink("", route, id);
		routerLink.setQueryParameters(QueryParameters.simple(Map.of(paramName, paramValue)));
		routerLink.add(component);
		return routerLink;
	}
}
