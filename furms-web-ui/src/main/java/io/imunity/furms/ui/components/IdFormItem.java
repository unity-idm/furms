/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public class IdFormItem extends FormLayout.FormItem {

	private final Div idField;
	private final Label label;

	public IdFormItem(String textLabel) {
		this.idField = new Div();
		this.label = new Label(textLabel);

		add(idField);
		addToLabel(label);
	}

	public IdFormItem(String id, String textLabel) {
		this(textLabel);
		setIdAndShow(id);
	}

	public void setIdAndShow(String id) {
		idField.setText(id);
		setVisible(!isEmpty(id));
	}

	public void setVisible(boolean visible) {
		idField.setVisible(visible);
		label.setVisible(visible);
	}
}
