/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DebounceSettings;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.combobox.ComboBox;

import java.util.function.Consumer;

import static com.vaadin.flow.dom.DebouncePhase.INTERMEDIATE;
import static com.vaadin.flow.dom.DebouncePhase.LEADING;
import static com.vaadin.flow.dom.DebouncePhase.TRAILING;

class CustomValueCheckBox<T> extends ComboBox<T> {
	private String customValue;

	CustomValueCheckBox() {
		this.setAllowCustomValue(true);
	}
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void addCustomValueSetListener(Consumer<String> consumer) {
		this.addListener(CustomValueCheckBoxEvent.class, (ComponentEventListener) eventListener -> {
			customValue = getElement().getProperty("filter");
			consumer.accept(customValue);
		});
	}

	public String getCustomValue() {
		return customValue;
	}

	public void clearEmail() {
		customValue = "";
	}

	@DomEvent(value = "input", debounce = @DebounceSettings(timeout = 500, phases = {LEADING, INTERMEDIATE, TRAILING}))
	public static class CustomValueCheckBoxEvent<T extends ComboBox<?>> extends ComponentEvent<T> {
		public CustomValueCheckBoxEvent(T source, boolean fromClient) {
			super(source, fromClient);
		}
	}
}
