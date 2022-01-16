/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.dependency.CssImport;
import org.vaadin.gatanaso.MultiselectComboBox;

@CssImport(value = "./styles/components/big-multiselect-combo-box.css")
public class BigMultiSelectComboBox<T> extends MultiselectComboBox<T> {

	public BigMultiSelectComboBox() {
		setDefaults();
	}
	public BigMultiSelectComboBox(String label) {
		super(label);
		setDefaults();
	}

	private void setDefaults() {
		addClassName("big-multiselect-combo-box");
	}
}
