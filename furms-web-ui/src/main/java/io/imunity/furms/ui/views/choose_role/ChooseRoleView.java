/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.choose_role;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.components.FurmsSelect;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@Route("choose/role")
@PageTitle(key = "view.choose-role.title")
public class ChooseRoleView extends FurmsViewComponent {
	ChooseRoleView(RoleTranslator roleTranslator) {
		Map<ViewMode, List<FurmsViewUserContext>> data = roleTranslator.translateRolesToUserViewContexts();
		List<FurmsViewUserContext> collect = data.values().stream()
			.flatMap(Collection::stream)
			.collect(toList());
		if(collect.size() == 1) {
			UI.getCurrent().navigate(collect.get(0).viewMode.route);
			return;
		}
		FurmsSelect furmsSelect = new FurmsSelect(data);
		VerticalLayout layout =
			new VerticalLayout(new H4(getTranslation("view.choose-role.select")), furmsSelect);
		layout.setSizeFull();
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		getContent().add(layout);
		getContent().setSizeFull();
	}
}
