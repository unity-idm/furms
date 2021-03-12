/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.services;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.utils.ResourceGetter;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.util.Optional;
import java.util.function.Function;

import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "site/admin/service/form", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.service.form.page.title")
class InfraServiceFormView extends FurmsViewComponent {
	private final Binder<ServiceViewModel> binder = new BeanValidationBinder<>(ServiceViewModel.class);
	private final ServiceFormComponent serviceFormComponent;
	private final InfraServiceService infraServiceService;

	private BreadCrumbParameter breadCrumbParameter;

	InfraServiceFormView(InfraServiceService infraServiceService) {
		this.infraServiceService = infraServiceService;
		this.serviceFormComponent = new ServiceFormComponent(binder);
		Button saveButton = createSaveButton();
		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));
		Button cancelButton = createCloseButton();

		FormButtons buttons = new FormButtons(cancelButton, saveButton);
		getContent().add(serviceFormComponent, buttons);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.site-admin.service.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(InfraServicesView.class));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.site-admin.service.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid())
				saveService();
		});
		return saveButton;
	}

	private void saveService() {
		ServiceViewModel serviceViewModel = binder.getBean();
		InfraService infraService = InfraServiceViewModelMapper.map(serviceViewModel);
		OptionalException<Void> optionalException;
		if(infraService.id == null)
			optionalException = getResultOrException(() -> infraServiceService.create(infraService));
		else
			optionalException = getResultOrException(() -> infraServiceService.update(infraService));

		optionalException.getThrowable().ifPresentOrElse(
			throwable -> NotificationUtils.showErrorNotification(getTranslation("service.error.message")),
			() -> UI.getCurrent().navigate(InfraServicesView.class)
		);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {

		ServiceViewModel serviceViewModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> infraServiceService.findById(id)))
			.flatMap(Function.identity())
			.map(InfraServiceViewModelMapper::map)
			.orElseGet(() -> new ServiceViewModel(ResourceGetter.getCurrentResourceId()));

		String trans = parameter == null
			? "view.site-admin.service.form.parameter.new"
			: "view.site-admin.service.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		serviceFormComponent.setFormPools(serviceViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
