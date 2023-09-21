/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.data.binder.Binder;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceTypeId;
import io.imunity.furms.ui.community.allocations.CommunityAllocationComboBoxesModelsResolver;
import io.imunity.furms.ui.community.allocations.CommunityAllocationViewModel;
import io.imunity.furms.ui.components.DefaultNameField;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.support.models.SiteComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceCreditComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.api.constant.ValidationConst.MAX_ALLOCATION_NAME_LENGTH;

public class CommunityAllocationFormComponent extends Composite<Div> {

	private final Binder<CommunityAllocationViewModel> binder;
	private final CommunityAllocationComboBoxesModelsResolver resolver;

	private final ComboBox<SiteComboBoxModel> siteComboBox;
	private final ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox;
	private final ComboBox<ResourceCreditComboBoxModel> resourceCreditComboBox;
	private ResourceCreditComboBoxModel selectedCreditComboBoxModel;
	private final Label availableAmountLabel;
	private BigDecimal availableAmount;
	private final BigDecimalField amountField;
	private final DefaultNameField defaultNameField;
	private boolean editMode;

	CommunityAllocationFormComponent(Binder<CommunityAllocationViewModel> binder, CommunityAllocationComboBoxesModelsResolver resolver) {
		this.binder = binder;
		this.resolver = resolver;
		FormLayout formLayout = new FurmsFormLayout();

		defaultNameField = DefaultNameField.createLongDefaultNameField(MAX_ALLOCATION_NAME_LENGTH);
		formLayout.addFormItem(defaultNameField, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.name"));

		siteComboBox = new ComboBox<>();
		siteComboBox.setItems(resolver.getSites());
		siteComboBox.setItemLabelGenerator(SiteComboBoxModel::getName);
		formLayout.addFormItem(siteComboBox, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.site"));

		resourceTypeComboBox = new ComboBox<>();
		resourceTypeComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		siteComboBox.addValueChangeListener(event -> {
			SiteComboBoxModel value = Optional.ofNullable(event.getValue()).orElse(event.getOldValue());
			resourceTypeComboBox.setItems(resolver.getResourceTypes(value.getId()));
		});
		formLayout.addFormItem(resourceTypeComboBox, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.resource_type"));

		resourceCreditComboBox = new ComboBox<>();
		resourceCreditComboBox.setWidth("17em");
		resourceCreditComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		formLayout.addFormItem(resourceCreditComboBox, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.resource_credit"));

		availableAmountLabel = new Label();
		amountField = new BigDecimalField();
		amountField.setValueChangeMode(EAGER);
		amountField.setHelperComponent(availableAmountLabel);

		resourceTypeComboBox.addValueChangeListener(event -> {
			if (event.getValue() == null) {
				resourceCreditComboBox.setItems(Collections.emptyList());
				createUnitLabel(amountField, ResourceMeasureUnit.NONE);
				return;
			}
			ResourceTypeId resourceTypeId = event.getValue().id;
			Set<ResourceCreditComboBoxModel> resourceCredits = resolver.getResourceCredits(resourceTypeId);
			if (resourceCredits.isEmpty()) {
				resourceCreditComboBox.setInvalid(true);
				resourceCreditComboBox.setErrorMessage(getTranslation("view.fenix-admin.resource-credits-allocation.form.field.resource_credit.empty"));
			} else {
				resourceCreditComboBox.setInvalid(false);
				resourceCreditComboBox.setItems(resourceCredits);
			}
			createUnitLabel(amountField, event.getValue().unit);
		});
		formLayout.addFormItem(amountField, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.amount"));

		resourceCreditComboBox.addValueChangeListener(event ->
			Optional.ofNullable(event.getValue()).ifPresentOrElse(
				x -> {
					selectedCreditComboBoxModel = x;
					reloadAvailableAmount();
				},
				() -> availableAmountLabel.setText("")
			)
		);

		prepareValidator(defaultNameField, siteComboBox, resourceTypeComboBox, resourceCreditComboBox, amountField);

		getContent().add(formLayout);
	}

	public void reloadAvailableAmount() {
		availableAmount = resolver.getAvailableAmount(selectedCreditComboBoxModel.id);
		availableAmountLabel.setText(getTranslation(selectedCreditComboBoxModel.split ?
						"view.fenix-admin.resource-credits-allocation.form.label.available" :
						"view.fenix-admin.resource-credits-allocation.form.label.availableNotSplit",
				availableAmount));
		amountField.setReadOnly(!selectedCreditComboBoxModel.split);
		if (!editMode)
			amountField.setValue(availableAmount);
	}

	public void reloadDefaultName(){
		defaultNameField.generateName();
	}

	public boolean isNameDefault(){
		return defaultNameField.isReadOnly();
	}

	private void createUnitLabel(BigDecimalField amountField, ResourceMeasureUnit unit) {
		amountField.setSuffixComponent(new Label(unit == null ? "" : unit.getSuffix()));
	}

	private void prepareValidator(DefaultNameField nameField, ComboBox<SiteComboBoxModel> siteComboBox,
	                              ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox,
	                              ComboBox<ResourceCreditComboBoxModel> resourceCreditComboBox, BigDecimalField amountField) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.field.name")
			)
			.bind(CommunityAllocationViewModel::getName, CommunityAllocationViewModel::setName);
		binder.forField(siteComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.combo-box.site")
			)
			.bind(
				CommunityAllocationViewModel::getSite,
				CommunityAllocationViewModel::setSite
			);
		binder.forField(resourceTypeComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.combo-box.resource-type")
			)
			.bind(
				CommunityAllocationViewModel::getResourceType,
				CommunityAllocationViewModel::setResourceType
			);
		binder.forField(resourceCreditComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.combo-box.resource-credit")
			)
			.bind(
				CommunityAllocationViewModel::getResourceCredit,
				CommunityAllocationViewModel::setResourceCredit
			);
		binder.forField(amountField)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.field.amount")
			)
			.withValidator(
				obj -> isAmountCorrect(resourceCreditComboBox, obj),
				getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.field.amount.range")
			)
			.bind(
				CommunityAllocationViewModel::getAmount,
				CommunityAllocationViewModel::setAmount
			);
	}

	private boolean isAmountCorrect(ComboBox<ResourceCreditComboBoxModel> resourceCreditComboBox, BigDecimal current) {
		Optional<ResourceCreditComboBoxModel> value = Optional.ofNullable(resourceCreditComboBox.getValue());
		if (value.isEmpty())
			return false;
		if (BigDecimal.ZERO.equals(current))
			return false;
		if (!value.get().split)
			return value.get().amount.compareTo(current) == 0;
		return availableAmount.compareTo(current) >= 0;
	}

	public void setModelObject(CommunityAllocationViewModel model, Supplier<Set<String>> occupiedNameGetter) {
		if(model.getSite() != null) {
			siteComboBox.setReadOnly(true);
			siteComboBox.setItems(model.getSite());
		}
		if(model.getResourceType() != null){
			resourceTypeComboBox.setReadOnly(true);
			resourceTypeComboBox.setItems(model.getResourceType());
		}
		if(model.getResourceCredit() != null) {
			resourceCreditComboBox.setReadOnly(true);
			resourceCreditComboBox.setItems(model.getResourceCredit());
		}
		editMode = model.getId() != null;
		binder.setBean(model);
		defaultNameField.reloadName(model.getCommunityName(), occupiedNameGetter, !editMode, model.getName());
	}
}
