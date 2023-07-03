/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import io.imunity.furms.api.constant.ValidationConst;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.ui.components.DefaultNameField;
import io.imunity.furms.ui.components.FormTextField;
import io.imunity.furms.ui.components.FurmsDateTimePicker;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.utils.BigDecimalUtils;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.api.constant.ValidationConst.MAX_RESOURCE_CREDIT_NAME_LENGTH;
import static io.imunity.furms.ui.utils.BigDecimalUtils.isBigDecimalGreaterThen0;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_END_TIME;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_START_TIME;
import static java.util.Optional.ofNullable;

class ResourceCreditFormComponent extends Composite<Div> {
	private final Binder<ResourceCreditViewModel> binder;
	private final ResourceTypeComboBoxModelResolver resolver;

	private final FormLayout formLayout;
	private final DefaultNameField defaultNameField;
	private final FurmsDateTimePicker startTimePicker;
	private final FurmsDateTimePicker endTimePicker;
	private final ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox;

	ResourceCreditFormComponent(Binder<ResourceCreditViewModel> binder, ResourceTypeComboBoxModelResolver resolver) {
		this.binder = binder;
		this.resolver = resolver;
		this.formLayout = new FurmsFormLayout();

		defaultNameField = DefaultNameField.createLongDefaultNameField(MAX_RESOURCE_CREDIT_NAME_LENGTH);
		formLayout.addFormItem(defaultNameField, getTranslation("view.site-admin.resource-credits.form.field.name"));

		resourceTypeComboBox = new ComboBox<>();
		resourceTypeComboBox.setItems(resolver.getResourceTypes());
		resourceTypeComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		formLayout.addFormItem(resourceTypeComboBox,
				getTranslation("view.site-admin.resource-credits.form.combo-box.resource-types"));

		Checkbox splitCheckbox = new Checkbox(getTranslation("view.site-admin.resource-credits.form.check-box.split"));
		formLayout.addFormItem(splitCheckbox, "");

		TextField amountField = new FormTextField();
		amountField.setValueChangeMode(EAGER);
		amountField.setMaxLength(MAX_RESOURCE_CREDIT_NAME_LENGTH);
		resourceTypeComboBox.addValueChangeListener(event -> createUnitLabel(amountField, event.getValue().unit));
		resourceTypeComboBox.addValueChangeListener(event -> defaultNameField.generateName(event.getValue().name));
		formLayout.addFormItem(amountField, getTranslation("view.site-admin.resource-credits.form.field.amount"));

		startTimePicker = new FurmsDateTimePicker(() -> DEFAULT_START_TIME);
		formLayout.addFormItem(startTimePicker, getTranslation("view.site-admin.resource-credits.form.field.start-time"));

		endTimePicker = new FurmsDateTimePicker(() -> DEFAULT_END_TIME);
		formLayout.addFormItem(endTimePicker, getTranslation("view.site-admin.resource-credits.form.field.end-time"));

		prepareValidator(defaultNameField, resourceTypeComboBox, splitCheckbox, amountField, startTimePicker, endTimePicker);

		getContent().add(formLayout);
	}

	private void createUnitLabel(TextField amountField, ResourceMeasureUnit unit) {
		amountField.setSuffixComponent(new Label(unit.getSuffix()));
	}

	private void prepareValidator(DefaultNameField nameField, ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox,
	                              Checkbox splitCheckbox, TextField amountField,
	                              FurmsDateTimePicker startTimePicker, FurmsDateTimePicker endTimePicker) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.site-admin.resource-credits.form.error.validation.field.name")
			)
			.bind(ResourceCreditViewModel::getName, ResourceCreditViewModel::setName);
		binder.forField(resourceTypeComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.site-admin.resource-credits.form.error.validation.combo-box.resource-type")
			)
			.bind(
				resourceType -> resolver.getResourceType(resourceType.getResourceTypeId()),
				(resourceTypeViewModel, resourceType) -> resourceTypeViewModel.setResourceTypeId(resourceType.id)
			);
		binder.forField(splitCheckbox)
			.bind(ResourceCreditViewModel::getSplit, ResourceCreditViewModel::setSplit);
		binder.forField(amountField)
			.withValidator(
				value -> Objects.nonNull(value) && isBigDecimalGreaterThen0(value),
				getTranslation("view.site-admin.resource-credits.form.error.validation.field.amount")
			)
			.bind(resourceCredit -> BigDecimalUtils.toString(resourceCredit.getAmount()),
				(resourceCredit, value) -> resourceCredit.setAmount(BigDecimalUtils.toBigDecimal(value))
			);
		binder.forField(startTimePicker)
			.withValidator(
				time -> Objects.nonNull(time)
						&& ofNullable(endTimePicker.getValue()).map(c -> c.isAfter(time)).orElse(true),
				getTranslation("view.site-admin.resource-credits.form.error.validation.field.start-time")
			)
			.bind(ResourceCreditViewModel::getStartTime,
				ResourceCreditViewModel::setStartTime);
		binder.forField(endTimePicker)
			.withValidator(
				time -> Objects.nonNull(time)
						&& ofNullable(startTimePicker.getValue()).map(c -> c.isBefore(time)).orElse(true),
				getTranslation("view.site-admin.resource-credits.form.error.validation.field.end-time")
			)
			.bind(ResourceCreditViewModel::getEndTime,
				ResourceCreditViewModel::setEndTime);
	}

	public void reloadDefaultName(){
		defaultNameField.generateName();
	}

	public void setFormPools(ResourceCreditViewModel resourceCreditViewModel, boolean blockTimeChange, Supplier<Set<String>> occupiedNamesGetter) {
		defaultNameField.reloadName(resourceCreditViewModel.getResourceTypeName(), occupiedNamesGetter, resourceCreditViewModel.getId() == null, resourceCreditViewModel.getName());
		binder.setBean(resourceCreditViewModel);
		if(blockTimeChange){
			startTimePicker.setReadOnly(true);
			endTimePicker.setReadOnly(true);
		}
		if(resourceCreditViewModel.getId() != null)
			resourceTypeComboBox.setReadOnly(true);

		addIdFieldForEditForm(resourceCreditViewModel);
	}

	public boolean isNameDefault(){
		return defaultNameField.isReadOnly();
	}

	private void addIdFieldForEditForm(ResourceCreditViewModel resourceCreditViewModel) {
		if (resourceCreditViewModel != null && Objects.nonNull(resourceCreditViewModel.getId())) {
			Div id = new Div();
			id.setText(resourceCreditViewModel.getId().id.toString());
			Label idLabel = new Label(getTranslation("view.site-admin.resource-credits.form.field.id"));

			formLayout.addComponentAsFirst(new FormLayout.FormItem(idLabel, id));
		}
	}
}
