/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.fenix.logs;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;

class AuditLogMultiSelectComboBox<T> extends MultiSelectComboBox<T>
{

	AuditLogMultiSelectComboBox(String width) {
		getStyle().set("margin-top", "0.7em");
		getStyle().set("margin-right", "0.7em");
		getStyle().set("width", width);
	}
}
