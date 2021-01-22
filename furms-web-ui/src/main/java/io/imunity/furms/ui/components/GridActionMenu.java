/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import static com.vaadin.flow.component.icon.VaadinIcon.MENU;
import static io.imunity.furms.ui.utils.MenuComponentFactory.createActionButton;

import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;

@CssImport("./styles/components/grid-action-menu.css")
public class GridActionMenu extends ContextMenu {

	public GridActionMenu() {
		super(createActionButton(MENU));
		setOpenOnClick(true);
		addClassName("grid-action-menu");
	}

}
