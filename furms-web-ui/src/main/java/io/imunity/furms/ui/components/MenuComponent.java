/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.vaadin.flow.component.Component;

public class MenuComponent {

	public final Class<? extends Component> component;
	public final List<Class<? extends Component>> subViews;

	private MenuComponent(Class<? extends Component> menuComponent, List<Class<? extends Component>> subViews) {
		this.component = menuComponent;
		this.subViews = ImmutableList.copyOf(subViews);
	}

	public static Builder builder(Class<? extends Component> component) {
		return new Builder(component);
	}

	public static final class Builder {
		private Class<? extends Component> component;
		private List<Class<? extends Component>> subViews = Collections.emptyList();

		private Builder(Class<? extends Component> component) {
			this.component = component;
		}

		public Builder menu(Class<? extends Component> component) {
			this.component = component;
			return this;
		}

		@SafeVarargs
		public final Builder subViews(Class<? extends Component>... subViews) {
			this.subViews = Arrays.asList(subViews);
			return this;
		}

		public MenuComponent build() {
			return new MenuComponent(component, subViews);
		}
	}

}
