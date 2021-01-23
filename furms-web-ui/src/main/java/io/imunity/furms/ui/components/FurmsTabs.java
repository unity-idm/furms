/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

@CssImport("./styles/components/furms-tabs.css")
public class FurmsTabs extends Tabs {

	public FurmsTabs(Tab... tabs) {
		super(tabs);
		setClassName("furms-tabs");
	}

}
