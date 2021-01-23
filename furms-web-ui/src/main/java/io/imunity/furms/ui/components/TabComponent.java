/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.RouterLink;

public class TabComponent extends Tab {
	public final List<Class<? extends Component>> componentClass;

	public TabComponent(String text, MenuComponent menu){
		super(new RouterLink(text, menu.component));
		List<Class<? extends Component>> components = Lists.newArrayList(menu.component);
		components.addAll(menu.subViews);
		this.componentClass = Collections.unmodifiableList(components);
	}
}
