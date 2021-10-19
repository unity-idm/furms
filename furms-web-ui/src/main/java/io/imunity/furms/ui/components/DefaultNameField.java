/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

public class DefaultNameField extends CustomField<String> {
	private static final DateTimeFormatter DD_MM_YY_FORMAT = DateTimeFormatter.ofPattern("ddMMyy");
	private static final int MAX_NAME_SIZE = 20;
	public final TextField textField;
	public final Checkbox checkbox;
	private Registration registration;
	private String resourceName;
	private Optional<String> currentName;
	private Supplier<Set<String>> occupiedNamesGetter;

	public DefaultNameField() {
		textField = new TextField();
		textField.setValueChangeMode(EAGER);
		textField.setMaxLength(20);
		textField.getStyle().set("margin", "0");

		checkbox = new Checkbox(getTranslation("component.default-name-field.checkbox.text"));
		checkbox.addValueChangeListener(event -> textField.setReadOnly(event.getValue()));
		checkbox.getStyle().set("margin", "0");

		Div div = new Div(textField, checkbox);
		div.getStyle().set("display", "flex");
		div.getStyle().set("flex-direction", "column");
		add(div);
	}

	@Override
	protected String generateModelValue() {
		return textField.getValue();
	}

	@Override
	protected void setPresentationValue(String s) {
		textField.setValue(s);
	}

	@Override
	public boolean isReadOnly() {
		return textField.isReadOnly();
	}

	public void activeDefaultName(String resourceName, Supplier<Set<String>> occupiedNamesGetter, boolean initialValue, String currentName){
		this.currentName = Optional.ofNullable(currentName);
		this.resourceName = Optional.ofNullable(resourceName).orElse("");
		this.occupiedNamesGetter = occupiedNamesGetter;
		if(registration != null)
			registration.remove();
		registration = checkbox.addValueChangeListener(event -> {
			if(event.getValue())
				generateName();
		});
		checkbox.setValue(initialValue);
		textField.setReadOnly(initialValue);
	}

	public void generateName(String resourceName) {
		this.resourceName = resourceName;
		generateName();
	}

	public void generateName() {
		Set<String> names = occupiedNamesGetter.get();
		currentName.ifPresent(names::remove);
		String name;
		int i = 1;
		do {
			String timeAndIteration = ZonedDateTime.now().format(DD_MM_YY_FORMAT) + "-" + i;
			name = resourceName + "-" + timeAndIteration;
			if(name.length() > MAX_NAME_SIZE)
				name = resourceName.substring(0, MAX_NAME_SIZE - timeAndIteration.length() - 1) + "-" + timeAndIteration;
			i++;
		} while(names.contains(name));
		setValue(name);
	}
}
