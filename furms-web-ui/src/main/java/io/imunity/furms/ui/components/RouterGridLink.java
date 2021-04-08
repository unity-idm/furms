/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouterLink;
import org.springframework.util.StringUtils;

import java.util.Map;

@CssImport("./styles/components/router-grid-link.css")
public class RouterGridLink extends RouterLink {

	public RouterGridLink(Component component,
	                      String id,
	                      Class<? extends FurmsViewComponent> route,
	                      String paramName,
	                      String paramValue) {
		super("", route, id);
		addClassName("router-grid-link");
		if (!StringUtils.isEmpty(paramName))
			setQueryParameters(QueryParameters.simple(Map.of(paramName, paramValue)));
		add(component);
	}

	public RouterGridLink(MenuButton component,
	                      String id,
	                      Class<? extends FurmsViewComponent> route) {
		this(component, id, route, null, null);
	}

	public RouterGridLink(VaadinIcon iconType,
			String id,
			Class<? extends FurmsViewComponent> route) {
		this(new MenuButton(iconType), id, route, null, null);
	}
	
	public RouterGridLink(VaadinIcon iconType,
			String id,
			Class<? extends FurmsViewComponent> route,
			String paramName,
			String paramValue) {
		this(new MenuButton(iconType), id, route, paramName, paramValue);
	}

	public RouterGridLink(String label,
	                      String id,
	                      Class<? extends FurmsViewComponent> route,
	                      String paramName,
	                      String paramValue) {
		this(getLabel(label), id, route, paramName, paramValue);
	}

	private static Label getLabel(String s) {
		Label label = new Label(s);
		label.setClassName("router-label");
		return label;
	}
}
