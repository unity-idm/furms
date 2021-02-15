/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class FurmsUserComboBox extends VerticalLayout implements HasValue<FurmsUserComboBox, FurmsViewUserModel>, HasValue.ValueChangeEvent<FurmsViewUserModel> {
	private final ComboBox<FurmsViewUserModel>  comboBox = new ComboBox<>();

	public FurmsUserComboBox(List<FurmsViewUserModel> userModels) {
		comboBox.setItemLabelGenerator(user -> {
			String fullName = ofNullable(user.firstname)
				.map(value -> value + " ").orElse("")
				+ ofNullable(user.lastname).orElse("");
			return fullName.isBlank() ? user.email : fullName;
		});
		comboBox.setItems(userModels);
		Label emailLabel = new Label("placeholder");
		emailLabel.getStyle().set("visibility", "hidden");
		comboBox.addValueChangeListener(event -> Optional.ofNullable(event.getValue())
			.ifPresentOrElse(
				value -> {
					emailLabel.setText(value.email);
					emailLabel.getStyle().set("visibility", "visible");
				},
				() -> emailLabel.getStyle().set("visibility", "hidden"))
		);
		setSpacing(false);
		setMargin(false);
		getStyle().set("padding", "unset");
		add(comboBox, emailLabel);
	}
	
	public boolean hasValue() {
		return comboBox.getValue() != null;
	}

	@Override
	public void setValue(FurmsViewUserModel furmsViewUserModel) {
		comboBox.setValue(furmsViewUserModel);
	}

	@Override
	public HasValue<?, FurmsViewUserModel> getHasValue() {
		return comboBox;
	}

	@Override
	public boolean isFromClient() {
		return false;
	}

	@Override
	public FurmsViewUserModel getOldValue() {
		return comboBox.getEmptyValue();
	}

	public FurmsViewUserModel getValue(){
		return comboBox.getValue();
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super FurmsUserComboBox> valueChangeListener) {
		return comboBox.addValueChangeListener(e -> valueChangeListener.valueChanged(this));
	}

	public void clear(){
		comboBox.clear();
	}

	@Override
	public void setReadOnly(boolean b) {
		comboBox.setReadOnly(b);
	}

	@Override
	public boolean isReadOnly() {
		return comboBox.isReadOnly();
	}

	@Override
	public void setRequiredIndicatorVisible(boolean b) {
		comboBox.setRequiredIndicatorVisible(b);
	}

	@Override
	public boolean isRequiredIndicatorVisible() {
		return comboBox.isRequiredIndicatorVisible();
	}

	public void setItems(List<FurmsViewUserModel> users){
		comboBox.setItems(users);
	}

	public void setEnabled(boolean enabled){
		comboBox.setEnabled(enabled);
	}

	public void setClassName(String className){
		comboBox.setClassName(className);
	}
}
