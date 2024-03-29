/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components.layout;

import static io.imunity.furms.ui.components.layout.FurmsAppLayoutUtils.getPageTitle;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.RouterLink;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.MenuComponent;

class BreadCrumbComponent extends Composite<Div> {
	private final Stack<BreadCrumb> bredCrumbs = new Stack<>();
	private final List<MenuComponent> menuRouts;

	BreadCrumbComponent(List<MenuComponent> menuRouts){
		getContent().setId("breadcrumb");
		getContent().setSizeFull();
		this.menuRouts = menuRouts;
	}

	public void update(FurmsViewComponent component){
		Class<? extends FurmsViewComponent> componentClass = component.getClass();
		BreadCrumb route = new BreadCrumb(componentClass, component.getParameter().orElse(null));
		
		getSubview(componentClass).ifPresent(menuElement -> {
			if (bredCrumbs.isEmpty()) {
				// entering page directly from URL link
				bredCrumbs.push(new BreadCrumb(menuElement.component, null));
			}
		});

		if(isChangingMenuViews(componentClass)) {
			bredCrumbs.removeAllElements();
		}
		
		if(!bredCrumbs.contains(route))
			bredCrumbs.push(route);
		else if (bredCrumbs.peek().isParamChanged(route)){
			bredCrumbs.pop();
			bredCrumbs.push(route);
		}
		else
			while (!bredCrumbs.peek().equals(route))
				bredCrumbs.pop();
		updateView();
	}

	private Optional<MenuComponent> getSubview(Class<? extends FurmsViewComponent> componentClass) {
		for (MenuComponent menu : menuRouts) {
			if (menu.subViews.contains(componentClass))
				return Optional.of(menu);
		}
		return Optional.empty();
	}

	private boolean isChangingMenuViews(Class<? extends FurmsViewComponent> componentClass) {
		return menuRouts.stream()
				.map(menu -> menu.component)
				.collect(toList())
				.contains(componentClass);
	}

	private void updateView() {
		List<Component> components = new ArrayList<>();
		RouterLink firstRouterLink = createRouterLink(bredCrumbs.firstElement()).findFirst()
				.orElseThrow(() -> new IllegalStateException("Expecting bread crumbs to contain at least one element: " 
						+ bredCrumbs));
		List<Component> nextComponents = bredCrumbs.stream()
			.skip(1)
			.flatMap(this::createNextRouterLink)
			.distinct()
			.collect(toList());
		components.add(firstRouterLink);
		components.addAll(nextComponents);

		getContent().removeAll();
		getContent().add(components.toArray(Component[]::new));
	}

	private Stream<Component> createNextRouterLink(BreadCrumb route) {
		return createRouterLink(route)
			.map(x -> {
				Span span = new Span(" > ");
				span.add(x);
				return span;
			});
	}

	private Stream<RouterLink> createRouterLink(BreadCrumb route) {
		return route.getBreadCrumbParameter()
			.map(p -> getRouterLink(route.getRouteClass(), p))
			.orElseGet(() -> Stream.of(new RouterLink(getPageTitle(route.getRouteClass()), route.getRouteClass())));
	}


	private Stream<RouterLink> getRouterLink(Class<? extends FurmsViewComponent> routeClass, BreadCrumbParameter p) {
		RouterLink basicRoute = new RouterLink(p.name, routeClass, p.id);
		if(p.parameter != null) {
			RouterLink routerLink = new RouterLink(p.parameter, routeClass, p.id);
			routerLink.setQueryParameters(QueryParameters.simple(Map.of("tab", p.parameter)));
			return Stream.of(basicRoute, routerLink);
		}
		return Stream.of(basicRoute);
	}
}
