/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.RouterLink;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

import static io.imunity.furms.ui.views.components.FurmsLayout.getPageTitle;
import static java.util.stream.Collectors.toList;

class BreadCrumbComponent extends Composite<Div> {
	private final Stack<Pair<Class<? extends FurmsViewComponent>, Optional<String>>> bredCrumbs = new Stack<>();
	private final List<Class<? extends Component>> menuRouts;

	BreadCrumbComponent(List<Class<? extends Component>> menuRouts){
		getContent().setId("breadcrumb");
		getContent().setSizeFull();
		this.menuRouts = menuRouts;
	}

	public void update(FurmsViewComponent component){
		Class<? extends FurmsViewComponent> componentClass = component.getClass();
		if(menuRouts.contains(componentClass))
			bredCrumbs.removeAllElements();

		Pair<Class<? extends FurmsViewComponent>, Optional<String>> route =
			Pair.of(componentClass, component.getParameter());
		if(!bredCrumbs.contains(route))
			bredCrumbs.push(route);
		else
			while (!bredCrumbs.peek().equals(route))
				bredCrumbs.pop();

		updateView();
	}

	private void updateView() {
		List<Component> components = new ArrayList<>();
		RouterLink firstRouterLink = createRouterLink(bredCrumbs.firstElement());
		List<Component> nextComponents = bredCrumbs.stream()
			.skip(1)
			.map(this::createNextRouterLink)
			.collect(toList());
		components.add(firstRouterLink);
		components.addAll(nextComponents);

		getContent().removeAll();
		getContent().add(components.toArray(Component[]::new));
	}

	private Component createNextRouterLink(Pair<Class<? extends FurmsViewComponent>, Optional<String>> route) {
		Span span = new Span(" > ");
		span.add(createRouterLink(route));
		return span;
	}

	private RouterLink createRouterLink(Pair<Class<? extends FurmsViewComponent>, Optional<String>> route) {
		return route.getValue()
			.map(v -> new RouterLink(v, route.getKey(), v))
			.orElseGet(() -> new RouterLink(getPageTitle(route.getKey()), route.getKey()));
	}
}
