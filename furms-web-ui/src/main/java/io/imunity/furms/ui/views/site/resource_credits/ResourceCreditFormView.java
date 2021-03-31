/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.domain.resource_credits.ResourceCredit;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.utils.ResourceGetter;
import io.imunity.furms.ui.views.site.SiteAdminMenu;

import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Function;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "site/admin/resource/credits/form", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.resource-credits.form.page.title")
class ResourceCreditFormView extends FurmsViewComponent {
	private final Binder<ResourceCreditViewModel> binder = new BeanValidationBinder<>(ResourceCreditViewModel.class);
	private final ResourceCreditFormComponent resourceCreditFormComponent;
	private final ResourceCreditService resourceCreditService;
	private ZoneId zoneId;

	private BreadCrumbParameter breadCrumbParameter;

	ResourceCreditFormView(ResourceCreditService resourceCreditService, ResourceTypeService resourceTypeService) {
		this.resourceCreditService = resourceCreditService;
		ResourceTypeComboBoxModelResolver resolver = new ResourceTypeComboBoxModelResolver(resourceTypeService.findAll(getCurrentResourceId()));
		this.resourceCreditFormComponent = new ResourceCreditFormComponent(binder, resolver);
		UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
			zoneId = ZoneId.of(extendedClientDetails.getTimeZoneId());
		});

		Button saveButton = createSaveButton();
		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));
		Button cancelButton = createCloseButton();

		FormButtons buttons = new FormButtons(cancelButton, saveButton);
		getContent().add(resourceCreditFormComponent, buttons);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.site-admin.resource-credits.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(ResourceCreditsView.class));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.site-admin.resource-credits.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid())
				saveResourceCredit();
		});
		return saveButton;
	}

	private void saveResourceCredit() {
		ResourceCreditViewModel serviceViewModel = binder.getBean();
		ResourceCredit resourceCredit = ResourceCreditViewModelMapper.map(serviceViewModel);
		OptionalException<Void> optionalException;
		if(resourceCredit.id == null)
			optionalException = getResultOrException(() -> resourceCreditService.create(resourceCredit));
		else
			optionalException = getResultOrException(() -> resourceCreditService.update(resourceCredit));

		optionalException.getThrowable().ifPresentOrElse(
			throwable -> NotificationUtils.showErrorNotification(getTranslation("resource-type.error.message")),
			() -> UI.getCurrent().navigate(ResourceCreditsView.class)
		);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		ResourceCreditViewModel serviceViewModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> resourceCreditService.findById(id)))
			.flatMap(Function.identity())
			.map(credit -> ResourceCreditViewModelMapper.map(credit, zoneId))
			.orElseGet(() -> new ResourceCreditViewModel(ResourceGetter.getCurrentResourceId()));

		String trans = parameter == null
			? "view.site-admin.resource-credits.form.parameter.new"
			: "view.site-admin.resource-credits.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		resourceCreditFormComponent.setFormPools(serviceViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}