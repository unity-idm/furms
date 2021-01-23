/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.landing;

import static io.imunity.furms.domain.constant.RoutesConst.CHOOSE_ROLE;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;

import io.imunity.furms.ui.components.FurmsSelect;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.LogoutIconFactory;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;

@Route(CHOOSE_ROLE)
@PageTitle(key = "view.landing.title")
public class LandingPageView extends FurmsViewComponent implements AfterNavigationObserver {
	private final Map<ViewMode, List<FurmsViewUserContext>> data;

	LandingPageView(RoleTranslator roleTranslator) {
		data = roleTranslator.translateRolesToUserViewContexts();

		Icon logout = LogoutIconFactory.create();
		HorizontalLayout logoutLayout = new HorizontalLayout(logout);
		logoutLayout.setWidthFull();
		logoutLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

		FurmsSelect furmsSelect = new FurmsSelect(data);
		VerticalLayout layout =
			new VerticalLayout(new H4(getTranslation("view.landing.select")), furmsSelect);
		layout.setSizeFull();
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		layout.setAlignItems(FlexComponent.Alignment.CENTER);

		getContent().add(logoutLayout, layout);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		List<FurmsViewUserContext> viewUserContexts = data.values().stream()
			.flatMap(Collection::stream)
			.collect(toList());
		if(viewUserContexts.size() == 1 || (viewUserContexts.size() == 2 && data.containsKey(ViewMode.USER))) {
			UI.getCurrent().getSession().setAttribute(FurmsViewUserContext.class, viewUserContexts.get(0));
			UI.getCurrent().navigate(viewUserContexts.get(0).route);
		}
	}
}
