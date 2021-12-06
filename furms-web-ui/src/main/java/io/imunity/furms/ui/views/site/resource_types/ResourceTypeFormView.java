/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

import java.util.Optional;
import java.util.function.Function;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.domain.resource_types.ResourceType;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

@Route(value = "site/admin/resource/types/form", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.resource-types.form.page.title")
class ResourceTypeFormView extends FurmsViewComponent {
	private final Binder<ResourceTypeViewModel> binder = new BeanValidationBinder<>(ResourceTypeViewModel.class);
	private final ResourceTypeFormComponent resourceTypeFormComponent;
	private final ResourceTypeService resourceTypeService;

	private BreadCrumbParameter breadCrumbParameter;

	ResourceTypeFormView(ResourceTypeService resourceTypeService,
			InfraServiceService serviceService,
			ResourceTypeDistributionChecker resourceTypeDistChecker) {
		this.resourceTypeService = resourceTypeService;
		ServiceComboBoxModelResolver resolver = new ServiceComboBoxModelResolver(serviceService.findAll(getCurrentResourceId()));
		this.resourceTypeFormComponent = new ResourceTypeFormComponent(binder, resolver, resourceTypeDistChecker);
		Button saveButton = createSaveButton();
		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));
		Button cancelButton = createCloseButton();

		FormButtons buttons = new FormButtons(cancelButton, saveButton);
		getContent().add(resourceTypeFormComponent, buttons);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.site-admin.resource-types.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(ResourceTypesView.class));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.site-admin.resource-types.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid())
				saveResourceType();
		});
		return saveButton;
	}

	private void saveResourceType() {
		ResourceTypeViewModel serviceViewModel = binder.getBean();
		ResourceType resourceType = ResourceTypeViewModelMapper.map(serviceViewModel);
		OptionalException<Void> optionalException;
		if(resourceType.id == null)
			optionalException = getResultOrException(() -> resourceTypeService.create(resourceType));
		else
			optionalException = getResultOrException(() -> resourceTypeService.update(resourceType));

		optionalException.getException().ifPresentOrElse(
			throwable -> NotificationUtils.showErrorNotification(getTranslation(throwable.getMessage())),
			() -> UI.getCurrent().navigate(ResourceTypesView.class)
		);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		ResourceTypeViewModel serviceViewModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> resourceTypeService.findById(id, getCurrentResourceId())))
			.flatMap(Function.identity())
			.map(ResourceTypeViewModelMapper::map)
			.orElseGet(() -> new ResourceTypeViewModel(getCurrentResourceId()));

		String trans = parameter == null
			? "view.site-admin.resource-types.form.parameter.new"
			: "view.site-admin.resource-types.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		resourceTypeFormComponent.setFormPools(serviceViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
