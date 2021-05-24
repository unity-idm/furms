/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.imunity.furms.ui.components.FurmsFormLayout;

import java.util.Objects;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

class InfraServiceFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;
	private static final int MAX_DESCRIPTION_LENGTH = 510;

	private final Binder<InfraServiceViewModel> binder;
	private final FormLayout formLayout;

	InfraServiceFormComponent(Binder<InfraServiceViewModel> binder) {
		this.binder = binder;
		this.formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.site-admin.service.form.field.name"));

		TextArea descriptionField = new TextArea();
		descriptionField.setClassName("description-text-area");
		descriptionField.setValueChangeMode(EAGER);
		descriptionField.setMaxLength(MAX_DESCRIPTION_LENGTH);
		formLayout.addFormItem(descriptionField, getTranslation("view.site-admin.service.form.field.description"));

		prepareValidator(nameField, descriptionField);

		getContent().add(formLayout);
	}

	private void prepareValidator(TextField nameField, TextArea descriptionField) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.site-admin.service.form.error.validation.field.name")
			)
			.bind(InfraServiceViewModel::getName, InfraServiceViewModel::setName);
		binder.forField(descriptionField)
			.bind(InfraServiceViewModel::getDescription, InfraServiceViewModel::setDescription);
	}

	public void setFormPools(InfraServiceViewModel serviceViewModel) {
		binder.setBean(serviceViewModel);

		addIdFieldForEditForm(serviceViewModel);
	}

	private void addIdFieldForEditForm(InfraServiceViewModel serviceViewModel) {
		if (serviceViewModel!= null && isNotEmpty(serviceViewModel.getId())) {
			Div id = new Div();
			id.setText(serviceViewModel.getId());
			Label idLabel = new Label(getTranslation("view.site-admin.service.form.field.id"));

			formLayout.addComponentAsFirst(new FormLayout.FormItem(idLabel, id));
		}
	}
}
