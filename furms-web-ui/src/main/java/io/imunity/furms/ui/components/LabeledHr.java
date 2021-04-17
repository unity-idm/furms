/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;

@CssImport("./styles/components/labeled-hr.css")
public class LabeledHr extends Div {

	public LabeledHr(String text) {
		addClassName("labeled-hr");
		add(text);
	}

}
