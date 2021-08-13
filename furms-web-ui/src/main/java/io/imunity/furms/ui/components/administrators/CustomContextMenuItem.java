/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import io.imunity.furms.ui.components.MenuButton;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

class CustomContextMenuItem {
	public final Function<UserGridItem, MenuButton> buttonProvider;
	public final Consumer<UserGridItem> action;

	CustomContextMenuItem(Function<UserGridItem, MenuButton> buttonProvider, Consumer<UserGridItem> action) {
		this.buttonProvider = buttonProvider;
		this.action = action;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CustomContextMenuItem that = (CustomContextMenuItem) o;
		return Objects.equals(buttonProvider, that.buttonProvider) && Objects.equals(action, that.action);
	}

	@Override
	public int hashCode() {
		return Objects.hash(buttonProvider, action);
	}

	@Override
	public String toString() {
		return "CustomContextMenuItem{" +
			"buttonProvider=" + buttonProvider +
			", action=" + action +
			'}';
	}
}
