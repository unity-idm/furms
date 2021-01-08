/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.choose_role;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.authz.RoleTranslator;
import io.imunity.furms.ui.views.components.FurmsSelect;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

@Route("choose/role")
@PageTitle(key = "view.choose-role.title")
public class ChooseRoleView extends FurmsViewComponent {
	ChooseRoleView(RoleTranslator roleTranslator) {
		FurmsSelect furmsSelect = new FurmsSelect(roleTranslator.translateRolesToUserScopes());
		VerticalLayout layout =
			new VerticalLayout(new H4(getTranslation("view.choose-role.select")), furmsSelect);
		layout.setSizeFull();
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		getContent().add(layout);
		getContent().setSizeFull();
	}
}
