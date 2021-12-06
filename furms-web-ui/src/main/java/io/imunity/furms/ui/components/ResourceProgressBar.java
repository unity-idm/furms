/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.html.Div;

public class ResourceProgressBar extends Div {

	public ResourceProgressBar(int currentUsage, int threshold){
		currentUsage = Math.max(currentUsage, 0);
		currentUsage = Math.min(currentUsage, 100);
		threshold = Math.max(threshold, 0);
		threshold = Math.min(threshold, 100);

		Div currentUsageLine = new Div();
		currentUsageLine.setWidth(currentUsage + "%");
		currentUsageLine.getStyle().set("margin", "0");
		currentUsageLine.setHeightFull();
		currentUsageLine.getStyle().set("border-top-left-radius", "2px");
		currentUsageLine.getStyle().set("border-bottom-left-radius", "2px");
		if(currentUsage < threshold)
			currentUsageLine.getStyle().set("background-color", "var(--lumo-primary-color)");
		else
			currentUsageLine.getStyle().set("background-color", "darkorange");


		Div fillLine = new Div();
		int fillValue = currentUsage > threshold ? 0 : threshold - currentUsage;
		fillLine.setWidth(fillValue + "%");
		fillLine.setHeightFull();
		fillLine.getStyle().set("background-color", "whitesmoke");
		fillLine.getStyle().set("margin", "0");


		Div thresholdLine = new Div();
		int value = currentUsage > threshold ? 100 - currentUsage : 100 - threshold;
		thresholdLine.setWidth(value + "%");
		thresholdLine.setHeightFull();
		thresholdLine.getStyle().set("background-color", "blanchedalmond");
		thresholdLine.getStyle().set("margin", "0");
		thresholdLine.getStyle().set("border-top-right-radius", "2px");
		thresholdLine.getStyle().set("border-bottom-right-radius", "2px");

		add(currentUsageLine, fillLine, thresholdLine);
		setSizeFull();
		getStyle().set("display", "flex");
		getStyle().set("border", "2px solid gray");
		getStyle().set("background-color", "whitesmoke");
		getStyle().set("padding", "0.2em");
		getStyle().set("height", "20px");
	}
}
