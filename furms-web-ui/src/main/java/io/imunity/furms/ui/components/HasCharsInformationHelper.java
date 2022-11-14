/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.HasHelper;

public interface HasCharsInformationHelper extends HasHelper {
	default void setRemainingCharsInformation(int size, int maxLength) {
		int reaming = maxLength - size;
		this.getElement().setProperty("helperText", reaming + "/" + maxLength);
	}
}
