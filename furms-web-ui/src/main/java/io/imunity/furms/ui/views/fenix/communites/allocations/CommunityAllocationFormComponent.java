/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

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
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit.SiUnit;
import io.imunity.furms.ui.community.allocations.CommunityAllocationComboBoxesModelsResolver;
import io.imunity.furms.ui.community.allocations.CommunityAllocationViewModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceCreditComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.support.models.ComboBoxModel;

public class CommunityAllocationFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;

	private final Binder<CommunityAllocationViewModel> binder;

	private ComboBox<ComboBoxModel> siteComboBox;
	private ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox;
	private ComboBox<ResourceCreditComboBoxModel> resourceCreditComboBox;
	private Label availableAmountLabel;
	private BigDecimal availableAmount;
	private BigDecimal lastAmount = new BigDecimal("0");

	CommunityAllocationFormComponent(Binder<CommunityAllocationViewModel> binder, CommunityAllocationComboBoxesModelsResolver resolver) {
		this.binder = binder;
		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.name"));

		siteComboBox = new ComboBox<>();
		siteComboBox.setItems(resolver.getSites());
		siteComboBox.setItemLabelGenerator(ComboBoxModel::getName);
		formLayout.addFormItem(siteComboBox, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.site"));

		resourceTypeComboBox = new ComboBox<>();
		resourceTypeComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		siteComboBox.addValueChangeListener(event -> {
			ComboBoxModel value = Optional.ofNullable(event.getValue()).orElse(event.getOldValue());
			resourceTypeComboBox.setItems(resolver.getResourceTypes(value.getId()));
		});
		formLayout.addFormItem(resourceTypeComboBox, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.resource_type"));

		resourceCreditComboBox = new ComboBox<>();
		resourceCreditComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		formLayout.addFormItem(resourceCreditComboBox, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.resource_credit"));

		BigDecimalField amountField = new BigDecimalField();
		amountField.setValueChangeMode(EAGER);
		resourceTypeComboBox.addValueChangeListener(event -> {
			String id = Optional.ofNullable(event.getValue()).map(x -> x.id).orElse(null);
			ResourceMeasureUnit unit = Optional.ofNullable(event.getValue()).map(x -> x.unit).orElse(null);
			resourceCreditComboBox.setItems(resolver.getResourceCredits(id));
			createUnitLabel(amountField, unit);
		});
		formLayout.addFormItem(amountField, getTranslation("view.fenix-admin.resource-credits-allocation.form.field.amount"));

		availableAmountLabel = new Label();
		resourceCreditComboBox.addValueChangeListener(event ->
			Optional.ofNullable(event.getValue()).ifPresentOrElse(
				x -> {
					availableAmount = resolver.getAvailableAmount(x.id);
					availableAmountLabel.setText(getTranslation("view.fenix-admin.resource-credits-allocation.form.label.available") + availableAmount);
				},
				() -> availableAmountLabel.setText("")
			)
		);
		formLayout.addFormItem(availableAmountLabel, "");

		prepareValidator(nameField, siteComboBox, resourceTypeComboBox, resourceCreditComboBox, amountField);

		getContent().add(formLayout);
	}

	private void createUnitLabel(BigDecimalField amountField, ResourceMeasureUnit unit) {
		if(unit == null)
			amountField.setSuffixComponent(new Label(""));
		else if(!unit.equals(SiUnit.none))
			amountField.setSuffixComponent(new Label(unit.name()));
	}

	private void prepareValidator(TextField nameField, ComboBox<ComboBoxModel> siteComboBox,
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
		if(value.isEmpty())
			return false;
		if (BigDecimal.ZERO.equals(current))
			return false;
		if(!value.get().split)
			return value.get().amount.compareTo(current) == 0;
		return availableAmount.compareTo(current.subtract(lastAmount)) >= 0;
	}

	public void setFormPools(CommunityAllocationViewModel model) {
		if(model.getSite() != null) {
			siteComboBox.setEnabled(false);
			siteComboBox.setItems(model.getSite());
		}
		if(model.getResourceType() != null){
			resourceTypeComboBox.setEnabled(false);
			resourceTypeComboBox.setItems(model.getResourceType());
		}
		if(model.getResourceCredit() != null) {
			resourceCreditComboBox.setEnabled(false);
			resourceCreditComboBox.setItems(model.getResourceCredit());
		}
		if(model.getAmount() != null) {
			lastAmount = model.getAmount();
		}
		binder.setBean(model);
	}
}
