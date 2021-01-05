/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.communites;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.*;
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.domain.communities.Community;
import io.imunity.furms.ui.views.components.BreadCrumbParameter;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;

import java.util.*;

import static io.imunity.furms.ui.views.fenix_admin.communites.constant.CommunityConst.*;

@Route(value = "fenix/admin/community", layout = FenixAdminMenu.class)
@PageTitle(key = "view.community.page.title")
public class CommunityView extends FurmsViewComponent {
	private final CommunityService communityService;

	private final Tab defaultTab;
	private final Tabs tabs;
	private final Map<String, Tab> paramToTab = new HashMap<>();
	private final List<RouterLink> links = new ArrayList<>();

	private BreadCrumbParameter breadCrumbParameter;

	CommunityView(CommunityService communityService) {
		this.communityService = communityService;

		RouterLink adminsRouterLink = new RouterLink(getTranslation("view.community.tab.1"), CommunityView.class);
		adminsRouterLink.setQueryParameters(QueryParameters.simple(Map.of(PARAM, ADMINISTRATORS)));
		Tab administratorsTab = new Tab(adminsRouterLink);
		paramToTab.put(ADMINISTRATORS, administratorsTab);
		defaultTab = administratorsTab;
		links.add(adminsRouterLink);

		RouterLink allocRouterLink = new RouterLink(getTranslation("view.community.tab.2"), CommunityView.class);
		allocRouterLink.setQueryParameters(QueryParameters.simple(Map.of(PARAM, ALLOCATIONS)));
		Tab allocationsTab = new Tab(allocRouterLink);
		paramToTab.put(ALLOCATIONS, allocationsTab);
		links.add(allocRouterLink);

		Div page1 = new Div();
		page1.setText("Page#1");

		Div page2 = new Div();
		page2.setText("Page#2");
		page2.setVisible(false);

		Map<Tab, Component> tabsToPages = new HashMap<>();
		tabsToPages.put(administratorsTab, page1);
		tabsToPages.put(allocationsTab, page2);

		tabs = new Tabs(administratorsTab, allocationsTab);
		Div pages = new Div(page1, page2);

		tabs.addSelectedChangeListener(event -> {
			tabsToPages.values().forEach(page -> page.setVisible(false));
			Component selectedPage = tabsToPages.get(tabs.getSelectedTab());
			selectedPage.setVisible(true);

		});

		getContent().add(tabs, pages);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String communityId) {
		Community community = communityService.findById(communityId).orElseThrow(IllegalStateException::new);
		String param = event.getLocation()
			.getQueryParameters()
			.getParameters()
			.getOrDefault(PARAM, List.of(ADMINISTRATORS))
			.iterator().next();
		Tab tab = paramToTab.getOrDefault(param, defaultTab);
		tabs.setSelectedTab(tab);
		links.forEach(x -> x.setRoute(getClass(), communityId));
		breadCrumbParameter = new BreadCrumbParameter(community.getId(), community.getName(), param);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
