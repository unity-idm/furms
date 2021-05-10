/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.ui.components.FurmsFormLayout;

import java.util.Objects;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

class ResourceTypeFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;

	private final Binder<ResourceTypeViewModel> binder;
	private final ServiceComboBoxModelResolver resolver;
	ComboBox<ServiceComboBoxModel> servicesComboBox;

	ResourceTypeFormComponent(Binder<ResourceTypeViewModel> binder, ServiceComboBoxModelResolver resolver) {
		this.binder = binder;
		this.resolver = resolver;

		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.site-admin.resource-types.form.field.name"));

		servicesComboBox = new ComboBox<>();
		servicesComboBox.setItems(resolver.getServices());
		servicesComboBox.setItemLabelGenerator(service -> service.name);
		formLayout.addFormItem(servicesComboBox, getTranslation("view.site-admin.resource-types.form.combo-box.service"));

		ComboBox<ResourceMeasureType> typeComboBox = new ComboBox<>();
		typeComboBox.setItemLabelGenerator(resourceMeasureType -> getTranslation("enum.ResourceMeasureType." + resourceMeasureType.name()));
		typeComboBox.setItems(ResourceMeasureType.values());
		formLayout.addFormItem(typeComboBox, getTranslation("view.site-admin.resource-types.form.combo-box.type"));

		ComboBox<ResourceMeasureUnit> unitComboBox = new ComboBox<>();
		unitComboBox.setItemLabelGenerator(ResourceMeasureUnit::getName);
		typeComboBox.addValueChangeListener(event -> {
			unitComboBox.setItems(event.getValue().units);
		});
		formLayout.addFormItem(unitComboBox, getTranslation("view.site-admin.resource-types.form.combo-box.unit"));

		prepareValidator(nameField, servicesComboBox, typeComboBox, unitComboBox);

		getContent().add(formLayout);
	}

	private void prepareValidator(TextField nameField, ComboBox<ServiceComboBoxModel> servicesComboBox, ComboBox<ResourceMeasureType> typeComboBox, ComboBox<ResourceMeasureUnit> unitComboBox) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.site-admin.resource-types.form.error.validation.field.name")
			)
			.bind(ResourceTypeViewModel::getName, ResourceTypeViewModel::setName);
		binder.forField(servicesComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.site-admin.resource-types.form.error.validation.combo-box.service")
			)
			.bind(
				resourceType -> resolver.getService(resourceType.serviceId),
				(resourceTypeViewModel, serviceId) -> resourceTypeViewModel.setServiceId(serviceId.id)
			);
		binder.forField(typeComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.site-admin.resource-types.form.error.validation.combo-box.type")
			)
			.bind(ResourceTypeViewModel::getType, ResourceTypeViewModel::setType);
		binder.forField(unitComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.site-admin.resource-types.form.error.validation.combo-box.unit")
			)
			.bind(ResourceTypeViewModel::getUnit, ResourceTypeViewModel::setUnit);
	}

	public void setFormPools(ResourceTypeViewModel resourceTypeViewModel) {
		binder.setBean(resourceTypeViewModel);
		if(resourceTypeViewModel.serviceId != null)
			servicesComboBox.setEnabled(false);
	}
}
