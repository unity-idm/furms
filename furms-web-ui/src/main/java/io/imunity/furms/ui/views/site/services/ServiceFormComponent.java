/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.imunity.furms.ui.components.FurmsFormLayout;

import java.util.Objects;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

class ServiceFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;
	private static final int MAX_DESCRIPTION_LENGTH = 510;

	private final Binder<ServiceViewModel> binder;

	public ServiceFormComponent(Binder<ServiceViewModel> binder) {
		this.binder = binder;

		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.site-admin.service.form.field.name"));

		TextArea descriptionField = new TextArea();
		descriptionField.setClassName("description-text-area");
		descriptionField.setValueChangeMode(EAGER);
		descriptionField.setMaxLength(MAX_DESCRIPTION_LENGTH);
		formLayout.addFormItem(descriptionField, getTranslation("view.site-admin.service.form.field.description"));

		ComboBox<String> policyComboBox = new ComboBox<>();
		formLayout.addFormItem(policyComboBox, getTranslation("view.site-admin.service.form.field.policy"));

		prepareValidator(nameField, descriptionField, policyComboBox);

		getContent().add(formLayout);
	}

	private void prepareValidator(TextField nameField, TextArea descriptionField, ComboBox<String> policyComboBox) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.site-admin.service.form.error.validation.field.name")
			)
			.bind(ServiceViewModel::getName, ServiceViewModel::setName);
		binder.forField(descriptionField)
			.bind(ServiceViewModel::getDescription, ServiceViewModel::setDescription);
	}

	public void setFormPools(ServiceViewModel serviceViewModel) {
		binder.setBean(serviceViewModel);
	}
}
