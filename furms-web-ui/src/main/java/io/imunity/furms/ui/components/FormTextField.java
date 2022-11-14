/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.textfield.TextField;

public class FormTextField extends TextField implements HasCharsInformationHelper {

	public FormTextField() {
		addValueChangeListener(event -> setRemainingCharsInformation(event.getValue().length(), getMaxLength()));
	}

}
