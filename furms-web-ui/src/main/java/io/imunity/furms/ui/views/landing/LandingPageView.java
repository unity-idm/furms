/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.landing;

import static com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER;
import static io.imunity.furms.domain.constant.RoutesConst.LANDING_PAGE_URL;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;

import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;

import static io.imunity.furms.domain.constant.RoutesConst.LOGOUT_TRIGGER_URL;

@Route(LANDING_PAGE_URL)
@PageTitle(key = "view.landing.title")
@CssImport("./styles/views/landing-page.css")
public class LandingPageView extends FurmsViewComponent implements AfterNavigationObserver {

	private final Map<ViewMode, List<FurmsViewUserContext>> data;

	LandingPageView(RoleTranslator roleTranslator) {
		this.data = roleTranslator.translateRolesToUserViewContexts();

		final VerticalLayout[] linksBlocks = data.keySet().stream()
				.sorted()
				.map(viewMode -> addSelectBlock(viewMode, data))
				.toArray(VerticalLayout[]::new);

		final VerticalLayout layout = new VerticalLayout();
		layout.setDefaultHorizontalComponentAlignment(CENTER);

		layout.add(new H3(getTranslation("view.landing.title")));
		layout.add(linksBlocks);

		getContent().add(layout);
		getContent().setClassName("landing-page");
	}

	private VerticalLayout addSelectBlock(ViewMode viewMode, Map<ViewMode, List<FurmsViewUserContext>> data) {
		final VerticalLayout selectBlock = new VerticalLayout();
		final List<FurmsViewUserContext> userContexts = data.get(viewMode);

		if (viewMode.hasHeader()) {
			final H4 label = new H4(getTranslation(format("view.landing.role.%s", viewMode.name())));
			selectBlock.add(label);
		}

		final Button[] buttons = userContexts.stream()
				.map(this::createLink)
				.toArray(Button[]::new);
		selectBlock.add(buttons);

		selectBlock.setDefaultHorizontalComponentAlignment(CENTER);

		return selectBlock;
	}

	private Button createLink(FurmsViewUserContext userContext) {
		final Button link = new Button(userContext.name);
		link.setClassName("button-link");

		link.addClickListener(event -> {
			userContext.setAsCurrent();
			if(userContext.redirectable){
				UI.getCurrent().navigate(userContext.route);
			}
		});

		return link;
	}

	@Override
	public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
		List<FurmsViewUserContext> viewUserContexts = data.values().stream()
				.flatMap(Collection::stream)
				.collect(toList());
		if (viewUserContexts.size() == 0) {
			UI.getCurrent().getPage().setLocation(LOGOUT_TRIGGER_URL);
			return;
		}		
		if (viewUserContexts.size() == 1 || (viewUserContexts.size() == 2 && data.containsKey(ViewMode.USER))) {
			viewUserContexts.get(0).setAsCurrent();
			UI.getCurrent().navigate(viewUserContexts.get(0).route);
		}
	}
}
