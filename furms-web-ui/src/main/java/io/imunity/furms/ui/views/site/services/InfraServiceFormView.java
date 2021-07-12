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
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.api.services.InfraServiceService;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.services.InfraService;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "site/admin/service/form", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.service.form.page.title")
class InfraServiceFormView extends FurmsViewComponent {
	private final Binder<InfraServiceViewModel> binder = new BeanValidationBinder<>(InfraServiceViewModel.class);
	private final InfraServiceFormComponent serviceFormComponent;
	private final InfraServiceService infraServiceService;

	private BreadCrumbParameter breadCrumbParameter;

	InfraServiceFormView(InfraServiceService infraServiceService, PolicyDocumentService policyDocumentService) {
		this.infraServiceService = infraServiceService;
		Map<PolicyId, PolicyDto> policyDtos = policyDocumentService.findAllBySiteId(getCurrentResourceId())
			.stream()
			.map(policyDocument -> new PolicyDto(policyDocument.id, policyDocument.name))
			.collect(Collectors.toMap(x -> x.id, x -> x));
		this.serviceFormComponent = new InfraServiceFormComponent(binder, policyDtos);
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
		InfraServiceViewModel serviceViewModel = binder.getBean();
		InfraService infraService = InfraServiceViewModelMapper.map(serviceViewModel);
		OptionalException<Void> optionalException;
		if(infraService.id == null)
			optionalException = getResultOrException(() -> infraServiceService.create(infraService));
		else
			optionalException = getResultOrException(() -> infraServiceService.update(infraService));

		optionalException.getException().ifPresentOrElse(
			throwable -> NotificationUtils.showErrorNotification(getTranslation(throwable.getMessage())),
			() -> UI.getCurrent().navigate(InfraServicesView.class)
		);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {

		InfraServiceViewModel serviceViewModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> infraServiceService.findById(id, getCurrentResourceId())))
			.flatMap(Function.identity())
			.map(InfraServiceViewModelMapper::map)
			.orElseGet(() -> new InfraServiceViewModel(getCurrentResourceId()));

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
