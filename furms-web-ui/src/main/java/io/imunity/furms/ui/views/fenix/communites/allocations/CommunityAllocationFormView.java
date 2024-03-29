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
import io.imunity.furms.api.communites.CommunityService;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.domain.community_allocation.CommunityAllocationId;
import io.imunity.furms.domain.resource_credits.ResourceCreditId;
import io.imunity.furms.ui.community.allocations.CommunityAllocationComboBoxesModelsResolver;
import io.imunity.furms.ui.community.allocations.CommunityAllocationModelsMapper;
import io.imunity.furms.ui.community.allocations.CommunityAllocationViewModel;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.fenix.communites.CommunityAllocationErrors;
import io.imunity.furms.ui.views.fenix.communites.CommunityView;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "fenix/admin/communities/resource/credit/allocation/form", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.resource-credits-allocation.form.page.title")
class CommunityAllocationFormView extends FurmsViewComponent {
	private final Binder<CommunityAllocationViewModel> binder = new BeanValidationBinder<>(CommunityAllocationViewModel.class);
	private final CommunityAllocationFormComponent communityAllocationFormComponent;
	private final CommunityAllocationService communityAllocationService;
	private final CommunityService communityService;
	private CommunityId communityId;
	private BreadCrumbParameter breadCrumbParameter;

	CommunityAllocationFormView(SiteService siteService, 
			ResourceTypeService resourceTypeService, 
			ResourceCreditService resourceCreditService,
			CommunityService communityService,
			CommunityAllocationService communityAllocationService) {

		this.communityAllocationService = communityAllocationService;
		this.communityService = communityService;
		CommunityAllocationComboBoxesModelsResolver resolver = new CommunityAllocationComboBoxesModelsResolver(
			siteService.findAll(),
			resourceTypeService::findAll,
			resourceCreditService::findAllNotExpiredByResourceTypeId,
			this::getAvailableAmount
		);
		this.communityAllocationFormComponent = new CommunityAllocationFormComponent(binder, resolver);

		Button saveButton = createSaveButton();
		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));
		Button cancelButton = createCloseButton();

		FormButtons buttons = new FormButtons(cancelButton, saveButton);
		getContent().add(communityAllocationFormComponent, buttons);
	}

	private BigDecimal getAvailableAmount(ResourceCreditId resourceCreditId) {
		CommunityAllocationViewModel allocationViewModel = binder.getBean();
		return allocationViewModel.getId() == null ? 
				communityAllocationService.getAvailableAmountForNew(resourceCreditId) :
				communityAllocationService.getAvailableAmountForUpdate(resourceCreditId, 
						allocationViewModel.getId());
	}
	
	
	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.fenix-admin.resource-credits-allocation.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(CommunityView.class, communityId.id.toString()));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.fenix-admin.resource-credits-allocation.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid())
				saveCommunityAllocation();
		});
		return saveButton;
	}

	private void saveCommunityAllocation() {
		CommunityAllocationViewModel allocationViewModel = binder.getBean();
		CommunityAllocation communityAllocation = CommunityAllocationModelsMapper.map(allocationViewModel);
		OptionalException<Void> optionalException = getResultOrException(() ->{
					if (communityAllocation.id == null) {
						communityAllocationService.create(communityAllocation);
					} else {
						communityAllocationService.update(communityAllocation);
					}},
					CommunityAllocationErrors.KNOWN_ERRORS);

		optionalException.getException().ifPresentOrElse(
			throwable -> {
				communityAllocationFormComponent.reloadAvailableAmount();
				if(throwable.getCause() instanceof DuplicatedNameValidationError && communityAllocationFormComponent.isNameDefault()) {
					showErrorNotification(getTranslation("default.name.duplicated.error.message"));
					communityAllocationFormComponent.reloadDefaultName();
				}
				else
					showErrorNotification(getTranslation(throwable.getMessage()));
			},
			() -> UI.getCurrent().navigate(CommunityView.class, communityId.id.toString())
		);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		CommunityAllocationViewModel serviceViewModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> communityAllocationService.findByIdWithRelatedObjects(new CommunityAllocationId(id))))
			.flatMap(Function.identity())
			.map(CommunityAllocationModelsMapper::map)
			.orElseGet(() -> {
				CommunityId communityId = new CommunityId(event.getLocation()
					.getQueryParameters()
					.getParameters()
					.get("communityId")
					.iterator().next());
				return new CommunityAllocationViewModel(communityId, communityService.findById(communityId).get().getName());
			});

		this.communityId = serviceViewModel.getCommunityId();
		String trans = parameter == null
			? "view.fenix-admin.resource-credits-allocation.form.parameter.new"
			: "view.fenix-admin.resource-credits-allocation.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		communityAllocationFormComponent.setModelObject(serviceViewModel, () -> communityAllocationService.getOccupiedNames(communityId));
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
