/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import com.vaadin.flow.component.ComponentUtil;
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
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.resource_allocations.ResourceAllocationsGridItem;
import io.imunity.furms.ui.components.support.models.ComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.AllocationCommunityComboBoxModel;
import io.imunity.furms.ui.components.support.models.allocation.ResourceTypeComboBoxModel;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.community.DashboardView;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.vaadin.flow.component.Key.ESCAPE;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static java.math.BigDecimal.ZERO;
import static java.util.stream.Collectors.toSet;

@Route(value = "community/admin/dashboard/allocation", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.dashboard.allocate.page.title")
public class ProjectAllocationDashboardFormView extends FurmsViewComponent {

	private final static int MAX_NAME_LENGTH = 25;

	private final ProjectAllocationService projectAllocationService;
	private final ProjectService projectService;
	private final ResourceTypeService resourceTypeService;

	private final Binder<ProjectAllocationViewModel> binder;

	private BigDecimal availableAmount;

	public ProjectAllocationDashboardFormView(ProjectAllocationService projectAllocationService,
	                                          ProjectService projectService,
	                                          ResourceTypeService resourceTypeService) {
		this.projectAllocationService = projectAllocationService;
		this.projectService = projectService;
		this.resourceTypeService = resourceTypeService;
		this.binder = new BeanValidationBinder<>(ProjectAllocationViewModel.class);

		binder.setBean(createViewModel());

		addForm();
		addButtons();
	}

	private ProjectAllocationViewModel createViewModel() {
		final ResourceAllocationsGridItem item = ComponentUtil.getData(UI.getCurrent(), ResourceAllocationsGridItem.class);
		final ResourceType type = resourceTypeService.findById(item.getResourceTypeId())
				.orElseThrow();
		ComponentUtil.setData(UI.getCurrent(), ResourceAllocationsGridItem.class, null);
		return ProjectAllocationViewModel.builder()
				.communityId(item.getCommunityId())
				.resourceType(new ResourceTypeComboBoxModel(type.id, type.name, type.unit))
				.allocationCommunity(new AllocationCommunityComboBoxModel(item.getId(), item.getName(),
						item.isSplit(), item.getCredit().getUnit()))
				.build();
	}

	private void addForm() {
		final FormLayout formLayout = new FurmsFormLayout();

		final Label availableAmountLabel = new Label();

		formLayout.addFormItem(nameField(),
				getTranslation("view.community-admin.project-allocation.form.field.name"));
		formLayout.addFormItem(projectsField(),
				getTranslation("view.community-admin.project-allocation.form.field.projects"));
		formLayout.addFormItem(resourceTypeField(),
				getTranslation("view.community-admin.project-allocation.form.field.resource_type"));
		formLayout.addFormItem(communityAllocation(availableAmountLabel),
				getTranslation("view.community-admin.project-allocation.form.field.community_allocation"));
		formLayout.addFormItem(amountField(),
				getTranslation("view.fenix-admin.resource-credits-allocation.form.field.amount"));
		formLayout.addFormItem(availableAmountLabel, "");

		getContent().add(formLayout);
	}

	private void addButtons() {
		final Button cancel = new Button(getTranslation("view.community-admin.project-allocation.form.button.cancel"));
		cancel.addThemeVariants(LUMO_TERTIARY);
		cancel.addClickShortcut(ESCAPE);
		cancel.addClickListener(event -> UI.getCurrent().navigate(DashboardView.class));

		final Button save = new Button(getTranslation("view.community-admin.project-allocation.form.button.save"));
		save.addThemeVariants(LUMO_PRIMARY);
		save.addClickListener(event -> {
			binder.validate();
			if (binder.isValid()) {
				saveProjectAllocation();
			}
		});

		binder.addStatusChangeListener(status -> save.setEnabled(binder.isValid()));

		getContent().add(new FormButtons(cancel, save));
	}

	private void saveProjectAllocation() {
		final ProjectAllocationViewModel viewModel = binder.getBean();
		final ProjectAllocation projectAllocation = ProjectAllocationModelsMapper.map(viewModel);
		final OptionalException<Void> optionalException =
				getResultOrException(() -> projectAllocationService.create(viewModel.getCommunityId(), projectAllocation));

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
						getTranslation("view.community-admin.project-allocation.form.error.validation.field.name"))
				.bind(ProjectAllocationViewModel::getName, ProjectAllocationViewModel::setName);

		return nameField;
	}

	private ComboBox<ComboBoxModel> projectsField() {
		final Set<ComboBoxModel> items = projectService.findAll(binder.getBean().getCommunityId()).stream()
				.map(item -> new ComboBoxModel(item.getId(), item.getName()))
				.collect(toSet());
		final ComboBox<ComboBoxModel> projectsComboBox = new ComboBox<>();
		projectsComboBox.setItemLabelGenerator(ComboBoxModel::getName);
		projectsComboBox.setItems(items);

		binder.forField(projectsComboBox)
				.withValidator(
						Objects::nonNull,
						getTranslation("view.community-admin.project-allocation.form.error.validation.combo-box.projects")
				)
				.bind(projectBinderGetter(items), projectBinderSetter());

		return projectsComboBox;
	}

	private ValueProvider<ProjectAllocationViewModel, ComboBoxModel> projectBinderGetter(Set<ComboBoxModel> items) {
		return (ValueProvider<ProjectAllocationViewModel, ComboBoxModel>) viewModel ->
				items.stream()
						.filter(item -> item.getId().equals(viewModel.getProjectId()))
						.findFirst().orElse(null);
	}

	private Setter<ProjectAllocationViewModel, ComboBoxModel> projectBinderSetter() {
		return (Setter<ProjectAllocationViewModel, ComboBoxModel>) (viewModel, comboBoxModel) ->
			viewModel.setProjectId(comboBoxModel.getId());
	}

	private ComboBox<ResourceTypeComboBoxModel> resourceTypeField() {
		final ComboBox<ResourceTypeComboBoxModel> resourceTypeComboBox = new ComboBox<>();
		resourceTypeComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		resourceTypeComboBox.setEnabled(false);
		resourceTypeComboBox.setItems(binder.getBean().getResourceType());

		binder.forField(resourceTypeComboBox)
				.bind(ProjectAllocationViewModel::getResourceType, ProjectAllocationViewModel::setResourceType);

		return resourceTypeComboBox;
	}

	private ComboBox<AllocationCommunityComboBoxModel> communityAllocation(Label availableAmountLabel) {
		final ComboBox<AllocationCommunityComboBoxModel> communityAllocationComboBox = new ComboBox<>();
		communityAllocationComboBox.setItemLabelGenerator(resourceType -> resourceType.name);
		communityAllocationComboBox.setEnabled(false);
		communityAllocationComboBox.setItems(binder.getBean().getAllocationCommunity());

		availableAmount = projectAllocationService.getAvailableAmount(binder.getBean().getCommunityId(),
				binder.getBean().getAllocationCommunity().id);
		availableAmountLabel.setText(createAvailableLabelContent());

		binder.forField(communityAllocationComboBox)
				.bind(ProjectAllocationViewModel::getAllocationCommunity, ProjectAllocationViewModel::setAllocationCommunity);

		return communityAllocationComboBox;
	}

	private String createAvailableLabelContent() {
		return getTranslation("view.community-admin.project-allocation.form.label.available") + availableAmount;
	}

	private BigDecimalField amountField() {
		final BigDecimalField amountField = new BigDecimalField();
		amountField.setValueChangeMode(EAGER);

		createUnitLabel(amountField, binder.getBean().getResourceType().unit);

		binder.forField(amountField)
				.withValidator(
						Objects::nonNull,
						getTranslation("view.community-admin.project-allocation.form.error.validation.field.amount")
				)
				.withValidator(
						this::isAmountCorrect,
						getTranslation("view.community-admin.project-allocation.form.error.validation.field.amount.range")
				)
				.bind(ProjectAllocationViewModel::getAmount, ProjectAllocationViewModel::setAmount);

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
		Optional<AllocationCommunityComboBoxModel> value = Optional.ofNullable(binder.getBean().getAllocationCommunity());
		if (value.isEmpty() || ZERO.equals(current)) {
			return false;
		}
		if (!value.get().split) {
			return availableAmount.compareTo(current) == 0;
		}
		return availableAmount.compareTo(current) >= 0;
	}

}