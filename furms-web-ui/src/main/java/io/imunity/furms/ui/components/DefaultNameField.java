/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.shared.Registration;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.api.constant.ValidationConst.MAX_ALLOCATION_NAME_LENGTH;

@CssImport(value="./styles/components/default-name-filed.css", themeFor="vaadin-text-field")
public class DefaultNameField extends CustomField<String> {
	private static final DateTimeFormatter DD_MM_YY_FORMAT = DateTimeFormatter.ofPattern("yyMMdd");
	private static final int LONG_GENERATED_NAME_LENGTH = 40;
	public final TextField textField;
	public final Checkbox checkbox;
	private Registration registration;
	private String resourceName;
	private Optional<String> currentName;
	private Supplier<Set<String>> occupiedNamesGetter;
	private final int generatedNameLength;

	public DefaultNameField(int generatedNameLength) {
		this.generatedNameLength = generatedNameLength;
		textField = new FormTextField();
		textField.setValueChangeMode(EAGER);
		textField.setMaxLength(generatedNameLength);
		textField.getStyle().set("margin", "0");
		textField.setReadOnly(true);

		checkbox = new Checkbox(getTranslation("component.default-name-field.checkbox.text"));
		checkbox.addValueChangeListener(event -> textField.setReadOnly(event.getValue()));
		checkbox.getStyle().set("margin", "0");
		checkbox.setValue(true);


		Div div = new Div(textField, checkbox);
		div.getStyle().set("display", "flex");
		div.getStyle().set("flex-direction", "column");
		add(div);
	}

	public static DefaultNameField createLongDefaultNameField(int maxLength) {
		DefaultNameField defaultNameField = new DefaultNameField(LONG_GENERATED_NAME_LENGTH);
		defaultNameField.setMaxLength(maxLength);
		defaultNameField.setClassName("long-default-name-field");
		return defaultNameField;
	}

	public void setMaxLength(int generatedNameSize) {
		textField.setMaxLength(generatedNameSize);
	}

	public void setClassName(String className) {
		textField.setClassName(className);
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

	@Override
	public void setReadOnly(boolean readOnly) {
		textField.setReadOnly(readOnly);
		checkbox.setValue(readOnly);
	}
	public void reloadName(String resourceName, Supplier<Set<String>> occupiedNamesGetter, boolean initialValue, String currentName) {
		textField.setReadOnly(initialValue);
		checkbox.setValue(initialValue);
		reloadName(resourceName, occupiedNamesGetter, currentName);
	}

	public void reloadName(String resourceName, Supplier<Set<String>> occupiedNamesGetter, String currentName) {
		this.currentName = Optional.ofNullable(currentName);
		this.resourceName = Optional.ofNullable(resourceName).orElse("");
		this.occupiedNamesGetter = occupiedNamesGetter;
		if(registration != null)
			registration.remove();
		registration = checkbox.addValueChangeListener(event -> {
			if(event.getValue())
				generateName();
		});
		if(checkbox.getValue())
			generateName();
	}

	public void generateName(String resourceName) {
		this.resourceName = resourceName;
		if(isReadOnly())
			generateName();
	}

	public void generateName() {
		if(resourceName.isEmpty())
			return;
		Set<String> names = occupiedNamesGetter.get();
		currentName.ifPresent(names::remove);
		String name;
		int i = 1;
		do {
			String timeAndIteration = ZonedDateTime.now().format(DD_MM_YY_FORMAT) + "-" + i;
			name = resourceName + "-" + timeAndIteration;
			if(name.length() > generatedNameLength)
				name = resourceName.substring(0, generatedNameLength - timeAndIteration.length() - 1) + "-" + timeAndIteration;
			i++;
		} while(names.contains(name));
		setValue(name);
	}
}
