/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix_admin.sites;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
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
import io.imunity.furms.ui.views.fenix_admin.sites.data.SiteDataAdd;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode.END;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;

@Route(value = "fenix/admin/sites/add", layout = FenixAdminMenu.class)
@PageTitle(key = "view.sites.add.page.title")
@CssImport("./styles/components/dropdown-menu.css")
public class SitesAddView extends FurmsViewComponent {

	private final SiteService siteService;

	private final VerticalLayout layout;

	private final Div errorMessage;

	SitesAddView(SiteService siteService) {
		this.siteService = siteService;

		layout = new VerticalLayout();
		layout.setPadding(true);
		layout.setSpacing(true);

		addHeader();

		errorMessage = new Div();
		errorMessage.getStyle().set("color", "red");
		layout.add(errorMessage);

		addForm();

		getContent().add(layout);
	}

	private void addHeader() {
		FlexLayout headerLayout = new FlexLayout();
		headerLayout.setWidthFull();

		H4 title = new H4("Create new Site");

		headerLayout.add(title);

		layout.add(headerLayout);
	}

	private void addForm() {
		FormLayout formLayout = new FormLayout();
		formLayout.setWidth("50%");
		SiteDataAdd formData = new SiteDataAdd();
		Binder<SiteDataAdd> binder = new Binder<>(SiteDataAdd.class);
		binder.setBean(formData);

		TextField name = new TextField();
		name.setPlaceholder("Site name...");
		name.setRequiredIndicatorVisible(true);
		name.setValueChangeMode(EAGER);
		name.setWidthFull();

		Button cancel = new Button("Cancel");
		cancel.addThemeVariants(LUMO_TERTIARY);
		cancel.addClickListener(e -> doCancelAction());

		Button save = new Button("Save");
		save.addThemeVariants(LUMO_PRIMARY);
		save.addClickListener(e -> doSaveAction(formData, binder));

		binder.forField(name)
				.withValidator(value -> value != null && !value.trim().isBlank(), "Site name has to be specified.")
				.bind(SiteDataAdd::getName, SiteDataAdd::setName);

		FlexLayout buttons = new FlexLayout(cancel, save);
		buttons.setJustifyContentMode(END);

		formLayout.addFormItem(name, "Name");
		formLayout.add(buttons);

		layout.add(formLayout);
	}

	private void doSaveAction(SiteDataAdd formData, Binder<SiteDataAdd> binder) {
		errorMessage.setText(null);
		binder.validate();
		if (binder.isValid()) {
			try {
				siteService.create(Site.builder()
						.name(formData.getName())
						.build());
				UI.getCurrent().navigate(SitesView.class);
			} catch (IllegalArgumentException exception) {
				errorMessage.setText(exception.getMessage());
			}
		}
	}

	private void doCancelAction() {
		UI.getCurrent().navigate(SitesView.class);
	}
}
