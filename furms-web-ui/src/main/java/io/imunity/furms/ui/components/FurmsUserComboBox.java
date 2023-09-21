/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.shared.Registration;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class FurmsUserComboBox extends CustomField<FurmsViewUserModel> {
	private final CustomValueCheckBox<FurmsViewUserModel>  comboBox = new CustomValueCheckBox<>();

	public FurmsUserComboBox(List<FurmsViewUserModel> userModels, boolean allowCustomValue) {
		comboBox.setItemLabelGenerator(user -> {
			String fullName = user.firstname
				.map(value -> value + " ").orElse("")
				+ user.lastname.orElse("");
			return fullName.isBlank() ? user.email : fullName;
		});
		comboBox.setItems(userModels);
		if(userModels.isEmpty())
			comboBox.setAutoOpen(false);
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
		if(allowCustomValue){
			comboBox.addFocusListener(event -> comboBox.setAllowCustomValue(true));
			comboBox.setPlaceholder(getTranslation("component.furms-user-comb-box.placeholder"));
		} else {
			comboBox.setAllowCustomValue(false);
		}
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

	public void addCustomValueSetListener(Consumer<String> consumer) {
		comboBox.addCustomValueSetListener(consumer);
	}

	public String getEmail(){
		return Optional.ofNullable(comboBox.getValue())
			.map(viewUserModel -> viewUserModel.email)
			.orElse(comboBox.getCustomValue());
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

	//Workaround - see https://github.com/vaadin/flow-components/issues/1798
	public void clearCustomValue(Button button){
		comboBox.clearEmail();
		comboBox.setAllowCustomValue(false);
		comboBox.focus();
		button.focus();
	}
}
