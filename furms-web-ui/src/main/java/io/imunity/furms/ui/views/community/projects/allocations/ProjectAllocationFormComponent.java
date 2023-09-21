/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.binder.Binder;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.ui.components.DefaultNameField;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.support.models.allocation.AllocationCommunityComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.api.constant.ValidationConst.MAX_ALLOCATION_NAME_LENGTH;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;

class ProjectAllocationFormComponent extends Composite<Div> {
	private final Binder<ProjectAllocationViewModel> binder;
	final ProjectAllocationComboBoxesModelsResolver resolver;

	private final ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox;
	private final ComboBox<AllocationCommunityComboBoxModel> communityAllocationComboBox;
	private final Label availableAmountLabel;
	private final DefaultNameField defaultNameField;
	private BigDecimal availableAmount;
	private BigDecimal lastAmount = new BigDecimal("0");

	ProjectAllocationFormComponent(Binder<ProjectAllocationViewModel> binder, ProjectAllocationComboBoxesModelsResolver resolver) {
		this.binder = binder;
		this.resolver = resolver;
		FormLayout formLayout = new FurmsFormLayout();

		defaultNameField = DefaultNameField.createLongDefaultNameField(MAX_ALLOCATION_NAME_LENGTH);
		formLayout.addFormItem(defaultNameField, getTranslation("view.community-admin.project-allocation.form.field.name"));

		resourceTypeComboBox = new ComboBox<>();
		resourceTypeComboBox.setItems(resolver.getResourceTypes());
		resourceTypeComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		formLayout.addFormItem(resourceTypeComboBox, getTranslation("view.community-admin.project-allocation.form.field.resource_type"));

		communityAllocationComboBox = new ComboBox<>();
		communityAllocationComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		communityAllocationComboBox.setWidth("17em");
		formLayout.addFormItem(communityAllocationComboBox, getTranslation("view.community-admin.project-allocation.form.field.community_allocation"));

		availableAmountLabel = new Label();
		BigDecimalField amountField = new BigDecimalField();
		amountField.setValueChangeMode(EAGER);
		amountField.setHelperComponent(availableAmountLabel);
		resourceTypeComboBox.addValueChangeListener(event -> {
			ResourceTypeId id =
				Optional.ofNullable(event.getValue()).map(x -> x.id).orElse(new ResourceTypeId((UUID) null));
			communityAllocationComboBox.setItems(resolver.getCommunityAllocations(id));
			createUnitLabel(amountField, null);
		});
		formLayout.addFormItem(amountField, getTranslation("view.community-admin.project-allocation.form.field.amount"));

		communityAllocationComboBox.addValueChangeListener(event ->
			Optional.ofNullable(event.getValue()).ifPresentOrElse(
				allocation -> {
					availableAmount = resolver.getAvailableAmount(new CommunityId(getCurrentResourceId()),
						allocation.id);
					availableAmountLabel.setText(getTranslation(allocation.split ? 
							"view.community-admin.project-allocation.form.label.available" :
							"view.community-admin.project-allocation.form.label.availableNotSplit", 
							availableAmount));
					createUnitLabel(amountField, allocation.unit);
					amountField.setReadOnly(!allocation.split);
					if(lastAmount.equals(BigDecimal.ZERO))
						amountField.setValue(availableAmount);
					else
						amountField.setValue(lastAmount);
				},
				() -> availableAmountLabel.setText("")
			)
		);

		prepareValidator(defaultNameField, resourceTypeComboBox, communityAllocationComboBox, amountField);

		getContent().add(formLayout);
	}

	private void createUnitLabel(BigDecimalField amountField, ResourceMeasureUnit unit) {
		amountField.setSuffixComponent(new Label(unit == null ? "" : unit.getSuffix()));
	}

	private void prepareValidator(DefaultNameField defaultNameField,
	                              ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox,
	                              ComboBox<AllocationCommunityComboBoxModel> resourceCreditComboBox, BigDecimalField amountField) {
		binder.forField(defaultNameField)
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

	public boolean isNameDefault(){
		return defaultNameField.isReadOnly();
	}

	public void reloadDefaultName(){
		defaultNameField.generateName();
	}

	public void setFormPools(ProjectAllocationViewModel model, Supplier<Set<String>> occupiedNamesGetter) {
		if(model.getAmount() != null)
			lastAmount = model.getAmount();
		binder.setBean(model);
		defaultNameField.reloadName(model.getProjectName(), occupiedNamesGetter, model.getId() == null, model.getName());
		if(model.getResourceType() != null)
			resourceTypeComboBox.setEnabled(false);
		else
			resourceTypeComboBox.setValue(resolver.getDefaultResourceType());
		if(model.getAllocationCommunity() != null)
			communityAllocationComboBox.setEnabled(false);
	}
}
