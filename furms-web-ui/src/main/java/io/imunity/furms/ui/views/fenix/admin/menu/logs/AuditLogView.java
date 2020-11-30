/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.admin.menu.logs;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.fenix.admin.menu.FenixAdminView;

@Route(value = "fenix/admin/auditLog", layout = FenixAdminView.class)
@PageTitle("Audit Log")
public class AuditLogView extends FurmsViewComponent {
	AuditLogView() {
		getContent().add(new Label("Placeholder"));
	}
}
