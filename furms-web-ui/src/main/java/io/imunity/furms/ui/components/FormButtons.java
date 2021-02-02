/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexLayout;

@CssImport("./styles/components/form-buttons.css")
public class FormButtons extends Div {
	
	public FormButtons(Button... buttons) {
		FlexLayout buttonsLayout = new FlexLayout(buttons);
		buttonsLayout.addClassName("form-buttons");
		add(buttonsLayout);
	}
}
