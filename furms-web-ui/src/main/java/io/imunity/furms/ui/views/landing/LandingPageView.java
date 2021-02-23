/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.landing;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import io.imunity.furms.ui.VaadinBroadcaster;
import io.imunity.furms.ui.components.FurmsSelect;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.imunity.furms.domain.constant.RoutesConst.LANDING_PAGE_URL;
import static java.util.stream.Collectors.toList;

@Route(LANDING_PAGE_URL)
@PageTitle(key = "view.landing.title")
class LandingPageView extends FurmsViewComponent implements AfterNavigationObserver {
	private final Map<ViewMode, List<FurmsViewUserContext>> data;

	LandingPageView(RoleTranslator roleTranslator, VaadinBroadcaster vaadinBroadcaster) {
		data = roleTranslator.translateRolesToUserViewContexts();
		RoleChooserLayout roleChooserLayout = new RoleChooserLayout(new FurmsSelect(roleTranslator, vaadinBroadcaster));
		getContent().add(roleChooserLayout);
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
