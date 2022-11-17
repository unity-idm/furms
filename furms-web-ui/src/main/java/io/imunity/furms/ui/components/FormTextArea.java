/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.textfield.TextArea;

public class FormTextArea extends TextArea implements HasCharsInformationHelper {

	public FormTextArea() {
		addValueChangeListener(event -> setRemainingCharsInformation(event.getValue().length(), getMaxLength()));
	}

	@Override
	public void setMaxLength(int maxLength) {
		super.setMaxLength(maxLength);
		setRemainingCharsInformation(getValue().length(), maxLength);
	}
}
