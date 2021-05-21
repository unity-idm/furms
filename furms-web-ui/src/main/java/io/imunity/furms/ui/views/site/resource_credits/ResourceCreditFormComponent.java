/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.BigDecimalUtils.isBigDecimal;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_END_TIME;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_START_TIME;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.ui.components.FurmsDateTimePicker;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.user_context.InvocationContext;
import io.imunity.furms.ui.utils.BigDecimalUtils;

class ResourceCreditFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;

	private final Binder<ResourceCreditViewModel> binder;
	private final ResourceTypeComboBoxModelResolver resolver;

	private final FormLayout formLayout;
	private ZoneId zoneId;

	ResourceCreditFormComponent(Binder<ResourceCreditViewModel> binder, ResourceTypeComboBoxModelResolver resolver) {
		this.binder = binder;
		this.resolver = resolver;
		zoneId = InvocationContext.getCurrent().getZone();
		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.site-admin.resource-credits.form.field.name"));

		ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox = new ComboBox<>();
		resourceTypeComboBox.setItems(resolver.getResourceTypes());
		resourceTypeComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		formLayout.addFormItem(resourceTypeComboBox,
				getTranslation("view.site-admin.resource-credits.form.combo-box.resource-types"));

		Checkbox splitCheckbox = new Checkbox(getTranslation("view.site-admin.resource-credits.form.check-box.split"));
		formLayout.addFormItem(splitCheckbox, "");

		Checkbox accessCheckbox = new Checkbox(getTranslation("view.site-admin.resource-credits.form.check-box.access"));
		formLayout.addFormItem(accessCheckbox, "");

		TextField amountField = new TextField();
		amountField.setValueChangeMode(EAGER);
		amountField.setMaxLength(MAX_NAME_LENGTH);
		resourceTypeComboBox.addValueChangeListener(event -> createUnitLabel(amountField, event.getValue().unit));
		formLayout.addFormItem(amountField, getTranslation("view.site-admin.resource-credits.form.field.amount"));

		FurmsDateTimePicker startTimePicker = new FurmsDateTimePicker(zoneId, () -> DEFAULT_START_TIME);
		formLayout.addFormItem(startTimePicker, getTranslation("view.site-admin.resource-credits.form.field.start-time"));

		FurmsDateTimePicker endTimePicker = new FurmsDateTimePicker(zoneId, () -> DEFAULT_END_TIME);
		formLayout.addFormItem(endTimePicker, getTranslation("view.site-admin.resource-credits.form.field.end-time"));

		prepareValidator(nameField, resourceTypeComboBox, splitCheckbox, accessCheckbox,
				amountField, startTimePicker, endTimePicker);

		getContent().add(formLayout);
	}

	private void createUnitLabel(TextField amountField, ResourceMeasureUnit unit) {
		amountField.setSuffixComponent(new Label(unit.getSuffix()));
	}

	private void prepareValidator(TextField nameField, ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox,
	                              Checkbox splitCheckbox, Checkbox accessCheckbox, TextField amountField,
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
		binder.forField(accessCheckbox)
			.bind(ResourceCreditViewModel::getAccess, ResourceCreditViewModel::setAccess);
		binder.forField(amountField)
			.withValidator(
				value -> Objects.nonNull(value) && isBigDecimal(value),
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
			.bind(credit -> ofNullable(credit.getStartTime()).map(ZonedDateTime::toLocalDateTime).orElse(null),
					(credit, startTime) -> credit.setStartTime(startTime.atZone(zoneId)));
		binder.forField(endTimePicker)
			.withValidator(
				time -> Objects.nonNull(time)
						&& ofNullable(startTimePicker.getValue()).map(c -> c.isBefore(time)).orElse(true),
				getTranslation("view.site-admin.resource-credits.form.error.validation.field.end-time")
			)
			.bind(credit -> ofNullable(credit.getEndTime()).map(ZonedDateTime::toLocalDateTime).orElse(null),
					(credit, endTime) -> credit.setEndTime(endTime.atZone(zoneId)));
	}

	public void setFormPools(ResourceCreditViewModel resourceCreditViewModel) {
		binder.setBean(resourceCreditViewModel);

		addIdFieldForEditForm(resourceCreditViewModel);
	}

	private void addIdFieldForEditForm(ResourceCreditViewModel resourceCreditViewModel) {
		if (resourceCreditViewModel != null && isNotEmpty(resourceCreditViewModel.getId())) {
			Div id = new Div();
			id.setText(resourceCreditViewModel.getId());
			Label idLabel = new Label(getTranslation("view.site-admin.resource-credits.form.field.id"));

			formLayout.addComponentAsFirst(new FormLayout.FormItem(idLabel, id));
		}
	}
}
