/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.choose_role;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

@Route("choose/role")
@PageTitle(key = "view.login-page.title")
public class ChooseRoleView extends FurmsViewComponent {
	ChooseRoleView() {
		ComboBox<Object> objectComboBox = new ComboBox<>();
		getContent().add(objectComboBox);
	}
}
