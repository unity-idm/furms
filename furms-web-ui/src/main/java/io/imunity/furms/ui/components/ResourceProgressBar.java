/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.html.Div;

public class ResourceProgressBar extends Div {

	public ResourceProgressBar(int currentUsage, int threshold) {
		currentUsage = Math.max(currentUsage, 0);
		currentUsage = Math.min(currentUsage, 100);
		threshold = Math.max(threshold, 0);
		threshold = Math.min(threshold, 99);

		getStyle().set("display", "flex");
		getStyle().set("border", "1px solid gray");
		getStyle().set("border-radius", "3px");
		getStyle().set("background-color", "whitesmoke");
		getStyle().set("padding", "0.1em");
		getStyle().set("height", "1.1em");

		Div currentUsageLine = getCurrentUsageDiv(currentUsage);
		add(currentUsageLine);

		if(threshold == 0)
			return;

		if(currentUsage < threshold) {
			Div linerDiv = getLinerDiv();
			linerDiv.setWidth(threshold - currentUsage + "%");
			linerDiv.getStyle().set("background-color", "whitesmoke");

			Div trianglesTagDiv = getTrianglesTagDiv();
			linerDiv.add(trianglesTagDiv);
			add(linerDiv);
		}
		else {
			currentUsageLine.getStyle().set("background-color", "darkorange");

			Div linerDiv = getLinerDiv();
			linerDiv.setWidth((float) threshold / currentUsage * 100 + "%");
			linerDiv.getStyle().set("background-color", "darkorange");

			Div trianglesTagDiv = getTrianglesTagDiv();
			linerDiv.add(trianglesTagDiv);
			currentUsageLine.add(linerDiv);
		}

		Div thresholdLine = getThresholdDiv(currentUsage, threshold);
		add(thresholdLine);
	}

	private Div getCurrentUsageDiv(int currentUsage) {
		Div currentUsageLine = new Div();
		currentUsageLine.setWidth(currentUsage + "%");
		currentUsageLine.getStyle().set("margin", "0");
		currentUsageLine.setHeightFull();
		currentUsageLine.getStyle().set("background-color", "var(--lumo-primary-color)");
		if(currentUsage == 100)
			currentUsageLine.getStyle().set("border-radius", "2px");
		else {
			currentUsageLine.getStyle().set("border-top-left-radius", "2px");
			currentUsageLine.getStyle().set("border-bottom-left-radius", "2px");
		}
		return currentUsageLine;
	}

	public Div getLinerDiv() {
		Div linerDiv = new Div();
		linerDiv.setHeightFull();
		linerDiv.getStyle().set("margin", "0");
		linerDiv.getStyle().set("display" ,"flex");
		linerDiv.getStyle().set("justify-content" ,"end");
		return linerDiv;
	}

	private Div getThresholdDiv(int currentUsage, int threshold) {
		Div thresholdLine = new Div();
		int value = currentUsage > threshold ? 100 - currentUsage : 100 - threshold;
		thresholdLine.setWidth(value + "%");
		thresholdLine.setHeightFull();
		thresholdLine.getStyle().set("background-color", "blanchedalmond");
		thresholdLine.getStyle().set("margin", "0");
		thresholdLine.getStyle().set("border-top-right-radius", "3px");
		thresholdLine.getStyle().set("border-bottom-right-radius", "3px");
		return thresholdLine;
	}

	private Div getTrianglesTagDiv() {
		Div triangles = new Div();
		triangles.getStyle().set("display" ,"flex");
		triangles.getStyle().set("justify-content" ,"space-between");
		triangles.getStyle().set("margin-right" ,"-0.2em");
		triangles.getStyle().set("flex-direction" ,"column");

		Div topTriangle = new Div();
		topTriangle.getStyle().set("clip-path", "polygon(50% 100%, 0 0, 100% 0)");
		topTriangle.getStyle().set("background-color", "red");
		topTriangle.getStyle().set("margin-top" ,"-0.1em");
		topTriangle.getStyle().set("width" ,"0.4em");
		topTriangle.getStyle().set("height" ,"0.3em");

		Div bottomTriangle = new Div();
		bottomTriangle.getStyle().set("clip-path", "polygon(0% 100%, 50% 0%, 100% 100%)");
		bottomTriangle.getStyle().set("background-color", "red");
		bottomTriangle.getStyle().set("margin-bottom" ,"-0.1em");
		bottomTriangle.getStyle().set("width" ,"0.4em");
		bottomTriangle.getStyle().set("height" ,"0.3em");
		triangles.add(topTriangle, bottomTriangle);
		return triangles;
	}
}
