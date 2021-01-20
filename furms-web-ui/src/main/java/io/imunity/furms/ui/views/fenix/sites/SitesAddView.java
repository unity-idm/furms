/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
import io.imunity.furms.ui.views.fenix.sites.data.SiteCreationParam;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

@Route(value = "fenix/admin/sites/add", layout = FenixAdminMenu.class)
@PageTitle(key = "view.sites.add.title")
public class SitesAddView extends FurmsViewComponent {

	private final SiteService siteService;

	private final TextField name;

	SitesAddView(SiteService siteService) {
		this.siteService = siteService;

		name = new TextField();

		addHeader();
		addForm();
	}

	private void addHeader() {
		FlexLayout headerLayout = new FlexLayout();
		headerLayout.setWidthFull();

		H4 title = new H4(getTranslation("view.sites.add.title"));

		headerLayout.add(title);

		getContent().add(headerLayout);
	}

	private void addForm() {
		FormLayout formLayout = new FormLayout();
		formLayout.setSizeFull();
		SiteCreationParam formData = new SiteCreationParam();
		Binder<SiteCreationParam> binder = new Binder<>(SiteCreationParam.class);
		binder.setBean(formData);

		name.setPlaceholder(getTranslation("view.sites.add.form.name.placeholder"));
		name.setRequiredIndicatorVisible(true);
		name.setValueChangeMode(EAGER);
		name.setWidthFull();

		Button cancel = new Button(getTranslation("view.sites.add.form.button.cancel"), e -> doCancelAction());
		cancel.addThemeVariants(LUMO_TERTIARY);
		cancel.addClassName("sites-add-form-button");

		Button save = new Button(getTranslation("view.sites.add.form.button.save"), e -> doSaveAction(formData, binder));
		save.addThemeVariants(LUMO_PRIMARY);
		save.addClickShortcut(Key.ENTER);
		save.addClassName("sites-add-form-button");

		binder.addStatusChangeListener(status -> save.setEnabled(!status.hasValidationErrors()));

		binder.forField(name)
				.withValidator(getNotEmptyStringValidator(), getTranslation("view.sites.form.error.validation.field.name.required"))
				.withValidator(siteService::isNameUnique, getTranslation("view.sites.form.error.validation.field.name.unique"))
				.bind(SiteCreationParam::getName, SiteCreationParam::setName);

		formLayout.setResponsiveSteps(
				new FormLayout.ResponsiveStep("1em", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
				new FormLayout.ResponsiveStep("60em", 2)
		);
		formLayout.addFormItem(name, getTranslation("view.sites.add.form.name"));

		FlexLayout buttons = new FlexLayout(cancel, save);
		buttons.setAlignContent(FlexLayout.ContentAlignment.START);

		getContent().add(formLayout, buttons);
	}

	private void doSaveAction(SiteCreationParam formData, Binder<SiteCreationParam> binder) {
		binder.validate();
		if (binder.isValid()) {
			try {
				siteService.create(Site.builder()
						.name(formData.getName())
						.build());
				UI.getCurrent().navigate(SitesView.class);
			} catch (IllegalArgumentException exception) {
				name.setErrorMessage(exception.getMessage());
				name.setInvalid(true);
			} catch (RuntimeException exception) {
				showErrorNotification(getTranslation("view.sites.form.error.unexpected", "save"));
			}
		}
	}

	private void doCancelAction() {
		UI.getCurrent().navigate(SitesView.class);
	}
}
