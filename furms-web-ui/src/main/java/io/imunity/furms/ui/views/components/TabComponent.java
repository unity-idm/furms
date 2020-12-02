/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.RouterLink;

public class TabComponent extends Tab {
	public final Class<? extends Component> componentClass;

	public TabComponent(String text, Class<? extends Component> componentClass){
		super(new RouterLink(text, componentClass));
		this.componentClass = componentClass;
	}
}
