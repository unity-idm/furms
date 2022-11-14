/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import io.imunity.furms.domain.resource_types.ResourceMeasureType;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.ui.components.FormTextField;
import io.imunity.furms.ui.components.FurmsFormLayout;

import java.util.Objects;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

class ResourceTypeFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;

	private final Binder<ResourceTypeViewModel> binder;
	private final ServiceComboBoxModelResolver resolver;
	private final ResourceTypeDistributionChecker resourceTypeDistChecker;

	private final FormLayout formLayout;
	private final ComboBox<ServiceComboBoxModel> servicesComboBox;
	private final Checkbox accessibleCheckbox;
	private final ComboBox<ResourceMeasureType> typeComboBox;
	private final ComboBox<ResourceMeasureUnit> unitComboBox;

	ResourceTypeFormComponent(Binder<ResourceTypeViewModel> binder,
			ServiceComboBoxModelResolver resolver,
			ResourceTypeDistributionChecker resourceTypeDistChecker) {
		this.binder = binder;
		this.resolver = resolver;
		this.resourceTypeDistChecker = resourceTypeDistChecker;

		this.formLayout = new FurmsFormLayout();

		TextField nameField = new FormTextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.site-admin.resource-types.form.field.name"));

		servicesComboBox = new ComboBox<>();
		servicesComboBox.setItems(resolver.getServices());
		servicesComboBox.setItemLabelGenerator(service -> service.name);
		formLayout.addFormItem(servicesComboBox, getTranslation("view.site-admin.resource-types.form.combo-box.service"));

		typeComboBox = new ComboBox<>();
		typeComboBox.setItemLabelGenerator(resourceMeasureType -> getTranslation("enum.ResourceMeasureType." + resourceMeasureType.name()));
		typeComboBox.setItems(ResourceMeasureType.values());
		formLayout.addFormItem(typeComboBox, getTranslation("view.site-admin.resource-types.form.combo-box.type"));

		unitComboBox = new ComboBox<>();
		unitComboBox.setItemLabelGenerator(ResourceMeasureUnit::name);
		typeComboBox.addValueChangeListener(event -> unitComboBox.setItems(event.getValue().units));
		formLayout.addFormItem(unitComboBox, getTranslation("view.site-admin.resource-types.form.combo-box.unit"));

		accessibleCheckbox = new Checkbox(getTranslation("view.site-admin.resource-types.form.checkbox.accessible"));
		formLayout.addFormItem(accessibleCheckbox, "");

		prepareValidator(nameField, servicesComboBox, typeComboBox, unitComboBox, accessibleCheckbox);

		getContent().add(formLayout);
	}

	private void prepareValidator(TextField nameField, ComboBox<ServiceComboBoxModel> servicesComboBox,
	                              ComboBox<ResourceMeasureType> typeComboBox, ComboBox<ResourceMeasureUnit> unitComboBox,
	                              Checkbox accessibleCheckbox) {
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
				resourceType -> resolver.getService(resourceType.getServiceId()),
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
		binder.forField(accessibleCheckbox)
			.bind(ResourceTypeViewModel::isAccessible, ResourceTypeViewModel::setAccessible);
	}

	public void setFormPools(ResourceTypeViewModel resourceTypeViewModel) {
		binder.setBean(resourceTypeViewModel);
		
		if (resourceTypeViewModel.getServiceId() != null) {
			servicesComboBox.setEnabled(false);
		}
		
		if (isFormEdit(resourceTypeViewModel)) {
			addResourcdTypeIdToForm(resourceTypeViewModel);
			disableFields(resourceTypeViewModel);
		}
	}

	private void disableFields(ResourceTypeViewModel resourceTypeViewModel) {

		accessibleCheckbox.setEnabled(false);

		if (resourceTypeDistChecker.isDistributed(resourceTypeViewModel)) {
			typeComboBox.setEnabled(false);
			unitComboBox.setEnabled(false);
		}
	}
	
	private void addResourcdTypeIdToForm(ResourceTypeViewModel resourceTypeViewModel) {
		Div id = new Div();
		id.setText(resourceTypeViewModel.getId().id.toString());
		Label idLabel = new Label(getTranslation("view.site-admin.resource-types.form.field.id"));
		formLayout.addComponentAsFirst(new FormLayout.FormItem(idLabel, id));
	}
	
	private boolean isFormEdit(ResourceTypeViewModel resourceTypeViewModel) {
		return resourceTypeViewModel != null && Objects.nonNull(resourceTypeViewModel.getId());
	}
}
