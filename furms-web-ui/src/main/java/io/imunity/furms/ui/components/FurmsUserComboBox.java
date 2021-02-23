/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class FurmsUserComboBox extends CustomField<FurmsViewUserModel> {
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
		VerticalLayout layout = new VerticalLayout(comboBox, emailLabel);
		layout.setSpacing(false);
		layout.setMargin(false);
		layout.setPadding(false);
		add(layout);
		getElement().getStyle().set("line-height", "unset");
	}

	@Override
	public FurmsViewUserModel getValue() {
		return comboBox.getValue();
	}

	@Override
	public void setValue(FurmsViewUserModel furmsViewUserModel) {
		comboBox.setValue(furmsViewUserModel);
	}

	@Override
	public Registration addValueChangeListener(ValueChangeListener<? super ComponentValueChangeEvent<CustomField<FurmsViewUserModel>, FurmsViewUserModel>> listener) {
		return comboBox.addValueChangeListener(e ->
			listener.valueChanged(new ComponentValueChangeEvent<>(
				this,
				this,
				comboBox.getEmptyValue(),
				false))
		);
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

	@Override
	protected FurmsViewUserModel generateModelValue() {
		return getValue();
	}

	@Override
	protected void setPresentationValue(FurmsViewUserModel furmsViewUserModel) {
		setValue(furmsViewUserModel);
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

	public boolean hasValue() {
		return comboBox.getValue() != null;
	}

	public void clear(){
		comboBox.clear();
	}
}
