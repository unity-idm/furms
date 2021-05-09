/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.dashboard;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.ui.community.allocations.CommunityAllocationModelsMapper;
import io.imunity.furms.ui.community.allocations.CommunityAllocationViewModel;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.resource_allocations.ResourceAllocationsGridItem;
import io.imunity.furms.ui.components.support.models.ComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceCreditComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

@Route(value = "fenix/admin/dashboard/allocate", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.dashboard.allocate.page.title")
class DashboardResourceAllocateFormView extends FurmsViewComponent {

	private final static int MAX_NAME_LENGTH = 25;

	private final CommunityAllocationService communityAllocationService;
	private final CommunityService communityService;
	private final ResourceTypeService resourceTypeService;

	private final Binder<CommunityAllocationViewModel> binder;

	private BigDecimal availableAmount;

	DashboardResourceAllocateFormView(CommunityAllocationService communityAllocationService,
	                                         CommunityService communityService,
	                                         ResourceTypeService resourceTypeService) {
		this.communityAllocationService = communityAllocationService;
		this.communityService = communityService;
		this.resourceTypeService = resourceTypeService;
		this.binder = new BeanValidationBinder<>(CommunityAllocationViewModel.class);

		binder.setBean(createViewModel());

		addForm();
		addButtons();
	}

	private CommunityAllocationViewModel createViewModel() {
		final ResourceAllocationsGridItem item = ComponentUtil.getData(UI.getCurrent(), ResourceAllocationsGridItem.class);
		final ResourceType type = resourceTypeService.findById(item.getResourceTypeId(), item.getSiteId())
				.orElseThrow();
		ComponentUtil.setData(UI.getCurrent(), ResourceAllocationsGridItem.class, null);
		return CommunityAllocationViewModel.builder()
				.site(new ComboBoxModel(item.getSiteId(), item.getSiteName()))
				.resourceType(new ResourceTypeComboBoxModel(type.id, type.name, type.unit))
				.resourceCredit(new ResourceCreditComboBoxModel(item.getId(), item.getName(),
						item.getCredit().getAmount(), item.isSplit()))
				.build();
	}

	private void addForm() {
		final FormLayout formLayout = new FurmsFormLayout();

		final Label availableAmountLabel = new Label();

		formLayout.addFormItem(nameField(),
				getTranslation("view.fenix-admin.resource-credits-allocation.form.field.name"));
		formLayout.addFormItem(siteField(),
				getTranslation("view.fenix-admin.resource-credits-allocation.form.field.site"));
		formLayout.addFormItem(communityField(),
				getTranslation("view.fenix-admin.resource-credits-allocation.form.field.community"));
		formLayout.addFormItem(resourceTypeField(),
				getTranslation("view.fenix-admin.resource-credits-allocation.form.field.resource_type"));
		formLayout.addFormItem(resourceCreditField(availableAmountLabel),
				getTranslation("view.fenix-admin.resource-credits-allocation.form.field.resource_type"));
		formLayout.addFormItem(amountField(),
				getTranslation("view.fenix-admin.resource-credits-allocation.form.field.amount"));
		formLayout.addFormItem(availableAmountLabel, "");

		getContent().add(formLayout);
	}

	private void addButtons() {
		final Button cancel = new Button(getTranslation("view.fenix-admin.resource-credits-allocation.form.button.cancel"));
		cancel.addThemeVariants(LUMO_TERTIARY);
		cancel.addClickShortcut(Key.ESCAPE);
		cancel.addClickListener(event -> UI.getCurrent().navigate(DashboardView.class));

		final Button save = new Button(getTranslation("view.fenix-admin.resource-credits-allocation.form.button.save"));
		save.addThemeVariants(LUMO_PRIMARY);
		save.addClickListener(event -> {
			binder.validate();
			if (binder.isValid()) {
				saveCommunityAllocation();
			}
		});

		binder.addStatusChangeListener(status -> save.setEnabled(binder.isValid()));

		getContent().add(new FormButtons(cancel, save));
	}

	private void saveCommunityAllocation() {
		final CommunityAllocationViewModel allocationViewModel = binder.getBean();
		final CommunityAllocation communityAllocation = CommunityAllocationModelsMapper.map(allocationViewModel);
		final OptionalException<Void> optionalException =
				getResultOrException(() -> communityAllocationService.create(communityAllocation));

		optionalException.getThrowable().ifPresentOrElse(
				throwable -> NotificationUtils.showErrorNotification(getTranslation(throwable.getMessage())),
				() -> UI.getCurrent().navigate(DashboardView.class)
		);
	}

	private TextField nameField() {
		final TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		binder.forField(nameField)
				.withValidator(
						value -> Objects.nonNull(value) && !value.isBlank(),
						getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.field.name"))
				.bind(CommunityAllocationViewModel::getName, CommunityAllocationViewModel::setName);

		return nameField;
	}

	private ComboBox<ComboBoxModel> siteField() {
		final ComboBox<ComboBoxModel> siteComboBox = new ComboBox<>();
		siteComboBox.setItemLabelGenerator(ComboBoxModel::getName);
		siteComboBox.setEnabled(false);
		siteComboBox.setItems(binder.getBean().getSite());

		binder.forField(siteComboBox)
				.bind(CommunityAllocationViewModel::getSite, CommunityAllocationViewModel::setSite);

		return siteComboBox;
	}

	private ComboBox<ComboBoxModel> communityField() {
		final Set<ComboBoxModel> items = communityService.findAll().stream()
				.map(item -> new ComboBoxModel(item.getId(), item.getName()))
				.collect(toSet());
		final ComboBox<ComboBoxModel> communityComboBox = new ComboBox<>();
		communityComboBox.setItemLabelGenerator(ComboBoxModel::getName);
		communityComboBox.setItems(items);

		binder.forField(communityComboBox)
				.withValidator(
						Objects::nonNull,
						getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.combo-box.community")
				)
				.bind(communityBinderGetter(items), communityBinderSetter());

		return communityComboBox;
	}

	private ValueProvider<CommunityAllocationViewModel, ComboBoxModel> communityBinderGetter(Set<ComboBoxModel> items) {
		return (ValueProvider<CommunityAllocationViewModel, ComboBoxModel>) viewModel ->
				items.stream()
						.filter(community -> community.getId().equals(viewModel.getCommunityId()))
						.findFirst().orElse(null);
	}

	private Setter<CommunityAllocationViewModel, ComboBoxModel> communityBinderSetter() {
		return (Setter<CommunityAllocationViewModel, ComboBoxModel>) (viewModel, comboBoxModel) ->
			viewModel.setCommunityId(comboBoxModel.getId());
	}

	private ComboBox<ResourceTypeComboBoxModel> resourceTypeField() {
		final ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox = new ComboBox<>();
		resourceTypeComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		resourceTypeComboBox.setEnabled(false);
		resourceTypeComboBox.setItems(binder.getBean().getResourceType());

		binder.forField(resourceTypeComboBox)
				.bind(CommunityAllocationViewModel::getResourceType, CommunityAllocationViewModel::setResourceType);

		return resourceTypeComboBox;
	}

	private ComboBox<ResourceCreditComboBoxModel> resourceCreditField(Label availableAmountLabel) {
		final ComboBox<ResourceCreditComboBoxModel> resourceCreditComboBox = new ComboBox<>();
		resourceCreditComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		resourceCreditComboBox.setEnabled(false);
		resourceCreditComboBox.setItems(binder.getBean().getResourceCredit());

		availableAmount = communityAllocationService.getAvailableAmount(binder.getBean().getResourceCredit().id);
		availableAmountLabel.setText(createAvailableLabelContent());

		binder.forField(resourceCreditComboBox)
				.bind(CommunityAllocationViewModel::getResourceCredit, CommunityAllocationViewModel::setResourceCredit);

		return resourceCreditComboBox;
	}

	private String createAvailableLabelContent() {
		return getTranslation("view.fenix-admin.resource-credits-allocation.form.label.available") + availableAmount;
	}

	private BigDecimalField amountField() {
		final BigDecimalField amountField = new BigDecimalField();
		amountField.setValueChangeMode(EAGER);

		createUnitLabel(amountField, binder.getBean().getResourceType().unit);

		binder.forField(amountField)
				.withValidator(
						Objects::nonNull,
						getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.field.amount")
				)
				.withValidator(
						this::isAmountCorrect,
						getTranslation("view.fenix-admin.resource-credits-allocation.form.error.validation.field.amount.range")
				)
				.bind(CommunityAllocationViewModel::getAmount, CommunityAllocationViewModel::setAmount);

		return amountField;
	}

	private void createUnitLabel(BigDecimalField amountField, ResourceMeasureUnit unit) {
		if (unit == null) {
			amountField.setSuffixComponent(new Label(""));
		} else if (!unit.equals(ResourceMeasureUnit.SiUnit.none)) {
			amountField.setSuffixComponent(new Label(unit.name()));
		}
	}

	private boolean isAmountCorrect(BigDecimal current) {
		Optional<ResourceCreditComboBoxModel> value = Optional.ofNullable(binder.getBean().getResourceCredit());
		if (value.isEmpty() || ZERO.equals(current)) {
			return false;
		}
		if (!value.get().split) {
			return value.get().amount.compareTo(current) == 0;
		}
		return availableAmount.compareTo(current) >= 0;
	}

}
