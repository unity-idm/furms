/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import java.io.ByteArrayInputStream;

@CssImport(value="./styles/components/menu-button-item.css", themeFor="vaadin-context-menu-list-box")
@CssImport(value="./styles/components/menu-button-item-color.css", themeFor="vaadin-button")
class ChartContextMenu extends Div {

	ChartContextMenu(ChartData chartData, byte[] jsonFile, byte[] csvFile) {
		ResourceAllocationChart.ChartGridActionMenu contextMenu = new ResourceAllocationChart.ChartGridActionMenu();

		Button jsonButton = new Button(getTranslation("chart.export.json"));
		jsonButton.setClassName("menu-button-color");
		Anchor jsonAnchor = new Anchor("", jsonButton);
		jsonAnchor.getElement().setAttribute("download", true);
		jsonAnchor.setHref(VaadinSession.getCurrent()
			.getResourceRegistry()
			.registerResource(new StreamResource(chartData.projectAllocationName + ".json", () -> new ByteArrayInputStream(jsonFile)))
			.getResourceUri()
			.toString()
		);

		Button csvButton = new Button((getTranslation("chart.export.csv")));
		csvButton.setClassName("menu-button-color");
		Anchor csvAnchor = new Anchor( "" , csvButton);
		csvAnchor.getElement().setAttribute("download", true);
		csvAnchor.setHref(VaadinSession.getCurrent()
			.getResourceRegistry()
			.registerResource(new StreamResource(chartData.projectAllocationName + ".csv", () -> new ByteArrayInputStream(csvFile)))
			.getResourceUri()
			.toString()
		);

		MenuItem jsonMenuItem = contextMenu.addItem(jsonAnchor);
		jsonMenuItem.getElement().setAttribute("id", "vaadin-menu-button");

		MenuItem csvMenuItem = contextMenu.addItem(csvAnchor);
		csvMenuItem.getElement().setAttribute("id", "vaadin-menu-button");

		Component target = contextMenu.getTarget();

		add(target);
		getStyle().set("margin-right", "0.6em");
	}
}
