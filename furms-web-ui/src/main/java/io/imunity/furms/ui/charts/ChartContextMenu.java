/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.charts;

import com.helger.commons.io.stream.StringInputStream;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

@CssImport(value="./styles/components/menu-chart-button.css", themeFor="vaadin-button")
class ChartContextMenu extends Div {

	ChartContextMenu(ChartData chartData, Supplier<String> jsonGetter, Supplier<String> csvGetter) {
		ResourceAllocationChart.ChartGridActionMenu contextMenu = new ResourceAllocationChart.ChartGridActionMenu();

		Button jsonButton = new Button(getTranslation("chart.export.json"));
		jsonButton.setClassName("menu-chart-button");
		Anchor jsonAnchor = new Anchor("", jsonButton);
		jsonAnchor.getElement().setAttribute("download", true);
		jsonAnchor.setHref(VaadinSession.getCurrent()
			.getResourceRegistry()
			.registerResource(new StreamResource(chartData.projectAllocationName + ".json", () -> new StringInputStream(jsonGetter.get(), UTF_8)))
			.getResourceUri()
			.toString()
		);

		Button csvButton = new Button((getTranslation("chart.export.csv")));
		csvButton.setClassName("menu-chart-button");
		Anchor csvAnchor = new Anchor( "" , csvButton);
		csvAnchor.getElement().setAttribute("download", true);
		csvAnchor.setHref(VaadinSession.getCurrent()
			.getResourceRegistry()
			.registerResource(new StreamResource(chartData.projectAllocationName + ".csv", () -> new StringInputStream(csvGetter.get(), UTF_8)))
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
