/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import io.imunity.furms.ui.components.MenuButton;

class CustomContextMenuItem<T> {
	public final Function<T, MenuButton> buttonProvider;
	public final Consumer<T> menuButtonHandler;
	public final Predicate<T> confirmer;

	CustomContextMenuItem(Function<T, MenuButton> buttonProvider, Consumer<T> menuButtonHandler, Predicate<T> confirmer) {
		this.buttonProvider = buttonProvider;
		this.menuButtonHandler = menuButtonHandler;
		this.confirmer = confirmer;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CustomContextMenuItem<?> that = (CustomContextMenuItem<?>) o;
		return Objects.equals(buttonProvider, that.buttonProvider) && Objects.equals(menuButtonHandler, that.menuButtonHandler);
	}

	@Override
	public int hashCode() {
		return Objects.hash(buttonProvider, menuButtonHandler);
	}

	@Override
	public String toString() {
		return "CustomContextMenuItem{" +
			"buttonProvider=" + buttonProvider +
			", action=" + menuButtonHandler +
			'}';
	}
}
