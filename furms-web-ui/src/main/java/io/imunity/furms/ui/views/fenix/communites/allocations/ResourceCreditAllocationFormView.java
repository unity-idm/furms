/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.resource_credit_allocation.ResourceCreditAllocationService;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.resource_credit_allocation.ResourceCreditAllocation;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.fenix.communites.CommunityView;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.util.Optional;
import java.util.function.Function;

import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

@Route(value = "site/admin/resource/credit/allocation/form", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.resource-credits-allocation.form.page.title")
class ResourceCreditAllocationFormView extends FurmsViewComponent {
	private final Binder<ResourceCreditAllocationViewModel> binder = new BeanValidationBinder<>(ResourceCreditAllocationViewModel.class);
	private final ResourceCreditAllocationFormComponent resourceCreditAllocationFormComponent;
	private final ResourceCreditAllocationService resourceCreditAllocationService;

	private String communityId;

	private BreadCrumbParameter breadCrumbParameter;

	ResourceCreditAllocationFormView(SiteService siteService, ResourceTypeService resourceTypeService, ResourceCreditService resourceCreditService, ResourceCreditAllocationService resourceCreditAllocationService) {
		this.resourceCreditAllocationService = resourceCreditAllocationService;
		ResourceCreditAllocationComboBoxesModelsResolver resolver = new ResourceCreditAllocationComboBoxesModelsResolver(
			siteService.findAll(),
			resourceTypeService::findAll,
			resourceCreditService::findAllByResourceTypeId
		);
		this.resourceCreditAllocationFormComponent = new ResourceCreditAllocationFormComponent(binder, resolver);

		Button saveButton = createSaveButton();
		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));
		Button cancelButton = createCloseButton();

		FormButtons buttons = new FormButtons(cancelButton, saveButton);
		getContent().add(resourceCreditAllocationFormComponent, buttons);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.fenix-admin.resource-credits-allocation.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(CommunityView.class, communityId));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.fenix-admin.resource-credits-allocation.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid())
				saveResourceCreditAllocation();
		});
		return saveButton;
	}

	private void saveResourceCreditAllocation() {
		ResourceCreditAllocationViewModel allocationViewModel = binder.getBean();
		ResourceCreditAllocation resourceCreditAllocation = ResourceCreditAllocationModelsMapper.map(allocationViewModel);
		OptionalException<Void> optionalException;
		if(resourceCreditAllocation.id == null)
			optionalException = getResultOrException(() -> resourceCreditAllocationService.create(resourceCreditAllocation));
		else
			optionalException = getResultOrException(() -> resourceCreditAllocationService.update(resourceCreditAllocation));

		optionalException.getThrowable().ifPresentOrElse(
			throwable -> NotificationUtils.showErrorNotification(getTranslation("resource-credit-allocation.error.message")),
			() -> UI.getCurrent().navigate(CommunityView.class, communityId)
		);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		ResourceCreditAllocationViewModel serviceViewModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> resourceCreditAllocationService.findByIdWithRelatedObjects(id)))
			.flatMap(Function.identity())
			.map(ResourceCreditAllocationModelsMapper::map)
			.orElseGet(ResourceCreditAllocationViewModel::new);

		String communityId = event.getLocation()
			.getQueryParameters()
			.getParameters()
			.getOrDefault("communityId", singletonList(serviceViewModel.communityId))
			.iterator().next();
		serviceViewModel.setCommunityId(communityId);
		this.communityId = communityId;

		String trans = parameter == null
			? "view.fenix-admin.resource-credits-allocation.form.parameter.new"
			: "view.fenix-admin.resource-credits-allocation.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		resourceCreditAllocationFormComponent.setFormPools(serviceViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
