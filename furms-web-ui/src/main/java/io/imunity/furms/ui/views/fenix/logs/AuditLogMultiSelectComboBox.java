/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.fenix.logs;

import com.vaadin.flow.component.dependency.CssImport;
import org.vaadin.gatanaso.MultiselectComboBox;

@CssImport(value = "./styles/components/audit-log-multiselect-combo-box-item.css", themeFor = "multiselect-combo-box")
class AuditLogMultiSelectComboBox<T> extends MultiselectComboBox<T> {

	AuditLogMultiSelectComboBox(String width) {
		setClassName("audit-log-multiselect-combo-box");
		getStyle().set("margin-top", "0.7em");
		getStyle().set("margin-right", "0.7em");
		getStyle().set("width", width);
	}
}
