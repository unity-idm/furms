/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.progressbar.ProgressBar;

import java.text.DecimalFormat;

import static java.lang.String.format;

@CssImport("./styles/components/progress-bar.css")
public class FurmsProgressBar extends Div {

	public FurmsProgressBar(double value) {
		super();
		addClassName("progressbar-container");

		final Label label = new Label(format("%.2f%%", value*100));
		label.addClassName("progressbar-label");

		final ProgressBar progressBar = new ProgressBar();
		progressBar.setValue(value);
		progressBar.addClassName("progressbar-value");

		add(label, progressBar);
	}
}
