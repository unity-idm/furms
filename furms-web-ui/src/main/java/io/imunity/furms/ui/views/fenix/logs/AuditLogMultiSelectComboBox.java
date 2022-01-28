/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.fenix.logs;

import org.vaadin.gatanaso.MultiselectComboBox;

class AuditLogMultiSelectComboBox<T> extends MultiselectComboBox<T> {

	AuditLogMultiSelectComboBox(String width) {
		getStyle().set("margin-top", "0.7em");
		getStyle().set("margin-right", "0.7em");
		getStyle().set("width", width);
	}
}
