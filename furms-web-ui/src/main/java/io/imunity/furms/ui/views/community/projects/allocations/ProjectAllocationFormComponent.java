/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.support.models.allocation.AllocationCommunityComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;

class ProjectAllocationFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;

	private final Binder<ProjectAllocationViewModel> binder;
	ProjectAllocationComboBoxesModelsResolver resolver;

	private ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox;
	private ComboBox<AllocationCommunityComboBoxModel> communityAllocationComboBox;
	private Label availableAmountLabel;
	private BigDecimal availableAmount;
	private BigDecimal lastAmount = new BigDecimal("0");

	ProjectAllocationFormComponent(Binder<ProjectAllocationViewModel> binder, ProjectAllocationComboBoxesModelsResolver resolver) {
		this.binder = binder;
		this.resolver = resolver;
		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.community-admin.project-allocation.form.field.name"));

		resourceTypeComboBox = new ComboBox<>();
		resourceTypeComboBox.setItems(resolver.getResourceTypes());
		resourceTypeComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		formLayout.addFormItem(resourceTypeComboBox, getTranslation("view.community-admin.project-allocation.form.field.resource_type"));

		communityAllocationComboBox = new ComboBox<>();
		communityAllocationComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		formLayout.addFormItem(communityAllocationComboBox, getTranslation("view.community-admin.project-allocation.form.field.community_allocation"));

		BigDecimalField amountField = new BigDecimalField();
		amountField.setValueChangeMode(EAGER);
		resourceTypeComboBox.addValueChangeListener(event -> {
			String id = Optional.ofNullable(event.getValue()).map(x -> x.id).orElse("");
			communityAllocationComboBox.setItems(resolver.getCommunityAllocations(id));
			createUnitLabel(amountField, null);
		});
		formLayout.addFormItem(amountField, getTranslation("view.community-admin.project-allocation.form.field.amount"));

		availableAmountLabel = new Label();
		communityAllocationComboBox.addValueChangeListener(event ->
			Optional.ofNullable(event.getValue()).ifPresentOrElse(
				allocation -> {
					availableAmount = resolver.getAvailableAmount(getCurrentResourceId(), allocation.id);
					availableAmountLabel.setText(getTranslation("view.community-admin.project-allocation.form.label.available") + availableAmount);
					createUnitLabel(amountField, allocation.unit);
				},
				() -> availableAmountLabel.setText("")
			)
		);
		formLayout.addFormItem(availableAmountLabel, "");

		prepareValidator(nameField, resourceTypeComboBox, communityAllocationComboBox, amountField);

		getContent().add(formLayout);
	}

	private void createUnitLabel(BigDecimalField amountField, ResourceMeasureUnit unit) {
		amountField.setSuffixComponent(new Label(unit == null ? "" : unit.getSuffix()));
	}

	private void prepareValidator(TextField nameField,
	                              ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox,
	                              ComboBox<AllocationCommunityComboBoxModel> resourceCreditComboBox, BigDecimalField amountField) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.community-admin.project-allocation.form.error.validation.field.name")
			)
			.bind(ProjectAllocationViewModel::getName, ProjectAllocationViewModel::setName);
		binder.forField(resourceTypeComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.community-admin.project-allocation.form.error.validation.combo-box.resource-type")
			)
			.bind(
				ProjectAllocationViewModel::getResourceType,
				ProjectAllocationViewModel::setResourceType
			);
		binder.forField(resourceCreditComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.community-admin.project-allocation.form.error.validation.combo-box.resource-credit")
			)
			.bind(
				ProjectAllocationViewModel::getAllocationCommunity,
				ProjectAllocationViewModel::setAllocationCommunity
			);
		binder.forField(amountField)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.community-admin.project-allocation.form.error.validation.field.amount")
			)
			.withValidator(
				obj -> isAmountCorrect(resourceCreditComboBox, obj),
				getTranslation("view.community-admin.project-allocation.form.error.validation.field.amount.range")
			)
			.bind(
				ProjectAllocationViewModel::getAmount,
				ProjectAllocationViewModel::setAmount
			);
	}

	private boolean isAmountCorrect(ComboBox<AllocationCommunityComboBoxModel> resourceCreditComboBox, BigDecimal current) {
		Optional<AllocationCommunityComboBoxModel> value = Optional.ofNullable(resourceCreditComboBox.getValue());
		if(value.isEmpty())
			return false;
		if (BigDecimal.ZERO.equals(current))
			return false;
		if(!value.get().split)
			return availableAmount.compareTo(current) == 0;
		return availableAmount.compareTo(current.subtract(lastAmount)) >= 0;
	}

	public void setFormPools(ProjectAllocationViewModel model) {
		binder.setBean(model);
		if(model.getResourceType() != null)
			resourceTypeComboBox.setEnabled(false);
		else
			resourceTypeComboBox.setValue(resolver.getDefaultResourceType());
		if(model.getAllocationCommunity() != null)
			communityAllocationComboBox.setEnabled(false);
		if(model.getAmount() != null)
			lastAmount = model.getAmount();
	}
}
