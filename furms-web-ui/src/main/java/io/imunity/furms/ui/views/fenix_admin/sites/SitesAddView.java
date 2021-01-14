/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.sites;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.menu.FenixAdminMenu;
import io.imunity.furms.ui.views.fenix_admin.sites.data.SiteCreationParam;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

@Route(value = "fenix/admin/sites/add", layout = FenixAdminMenu.class)
@PageTitle(key = "view.sites.add.title")
@CssImport("./styles/components/dropdown-menu.css")
public class SitesAddView extends FurmsViewComponent {

	private final SiteService siteService;

	private final VerticalLayout mainContent;

	private final TextField name;

	SitesAddView(SiteService siteService) {
		this.siteService = siteService;

		mainContent = new VerticalLayout();
		mainContent.setPadding(true);
		mainContent.setSpacing(true);

		name = new TextField();

		addHeader();

		addForm();

		getContent().add(mainContent);
	}

	private void addHeader() {
		FlexLayout headerLayout = new FlexLayout();
		headerLayout.setWidthFull();

		H4 title = new H4(getTranslation("view.sites.add.title"));

		headerLayout.add(title);

		mainContent.add(headerLayout);
	}

	private void addForm() {
		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("50%");
		SiteCreationParam formData = new SiteCreationParam();
		Binder<SiteCreationParam> binder = new Binder<>(SiteCreationParam.class);
		binder.setBean(formData);

		name.setPlaceholder(getTranslation("view.sites.add.form.name.placeholder"));
		name.setRequiredIndicatorVisible(true);
		name.setValueChangeMode(EAGER);
		name.setWidthFull();

		Button cancel = new Button(getTranslation("view.sites.add.form.button.cancel"));
		cancel.addThemeVariants(LUMO_TERTIARY);
		cancel.addClickListener(e -> doCancelAction());

		Button save = new Button(getTranslation("view.sites.add.form.button.save"));
		save.addThemeVariants(LUMO_PRIMARY);
		save.addClickListener(e -> doSaveAction(formData, binder));
		save.setEnabled(false);
		binder.addStatusChangeListener(status -> save.setEnabled(!status.hasValidationErrors()));

		binder.forField(name)
				.withValidator(getNotEmptyStringValidator(), getTranslation("view.sites.form.error.validation.field.name.required"))
				.withValidator(siteService::isNameUnique, getTranslation("view.sites.form.error.validation.field.name.unique"))
				.bind(SiteCreationParam::getName, SiteCreationParam::setName);

		FlexLayout buttons = new FlexLayout(cancel, save);
		buttons.setJustifyContentMode(END);

		formLayout.addFormItem(name, getTranslation("view.sites.add.form.name"));
		formLayout.add(buttons);

		mainContent.add(formLayout);
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
			}
		}
	}

	private void doCancelAction() {
		UI.getCurrent().navigate(SitesView.class);
	}
}
