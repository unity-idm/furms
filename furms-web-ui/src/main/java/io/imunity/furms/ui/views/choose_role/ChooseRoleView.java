/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.choose_role;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;
import io.imunity.furms.ui.views.components.FurmsSelect;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.imunity.furms.domain.constant.RoutesConst.CHOOSE_ROLE;
import static java.util.stream.Collectors.toList;

@Route(CHOOSE_ROLE)
@PageTitle(key = "view.choose-role.title")
public class ChooseRoleView extends FurmsViewComponent implements AfterNavigationObserver {
	private final Map<ViewMode, List<FurmsViewUserContext>> data;

	ChooseRoleView(RoleTranslator roleTranslator) {
		data = roleTranslator.translateRolesToUserViewContexts();
		FurmsSelect furmsSelect = new FurmsSelect(data);
		VerticalLayout layout =
			new VerticalLayout(new H4(getTranslation("view.choose-role.select")), furmsSelect);
		layout.setSizeFull();
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		getContent().add(layout);
		getContent().setSizeFull();
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		List<FurmsViewUserContext> viewUserContexts = data.values().stream()
			.flatMap(Collection::stream)
			.collect(toList());
		if(viewUserContexts.size() == 1) {
			UI.getCurrent().navigate(viewUserContexts.get(0).viewMode.route);
		}
	}
}
