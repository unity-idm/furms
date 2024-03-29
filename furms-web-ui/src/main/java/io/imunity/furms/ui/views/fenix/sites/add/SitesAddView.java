/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.sites.add;

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
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FormTextField;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;
import io.imunity.furms.ui.views.fenix.sites.SitesView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.api.constant.ValidationConst.MAX_SITE_NAME_LENGTH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Route(value = "fenix/admin/sites/add", layout = FenixAdminMenu.class)
@PageTitle(key = "view.sites.add.title")
public class SitesAddView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final SiteService siteService;

	private final TextField name;

	SitesAddView(SiteService siteService) {
		this.siteService = siteService;

		name = new FormTextField();
		name.focus();
		
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
		FormLayout formLayout = new FurmsFormLayout();
		formLayout.setWidthFull();
		SiteCreationParam formData = new SiteCreationParam();
		Binder<SiteCreationParam> binder = new Binder<>(SiteCreationParam.class);
		binder.setBean(formData);

		name.setPlaceholder(getTranslation("view.sites.add.form.name.placeholder"));
		name.setRequiredIndicatorVisible(true);
		name.setValueChangeMode(EAGER);
		name.setMaxLength(MAX_SITE_NAME_LENGTH);

		Button cancel = new Button(getTranslation("view.sites.add.form.button.cancel"), e -> doCancelAction());
		cancel.addThemeVariants(LUMO_TERTIARY);

		Button save = new Button(getTranslation("view.sites.add.form.button.save"), e -> doSaveAction(formData, binder));
		save.addThemeVariants(LUMO_PRIMARY);
		save.addClickShortcut(Key.ENTER);

		FormButtons buttons = new FormButtons(cancel, save);

		binder.addStatusChangeListener(status -> save.setEnabled(!isBlank(name.getValue()) && !status.hasValidationErrors()));

		binder.forField(name)
				.withValidator(getNotEmptyStringValidator(), getTranslation("view.sites.form.error.validation.field.name.required"))
				.withValidator(siteService::isNamePresent, getTranslation("view.sites.form.error.validation.field.name.unique"))
				.bind(SiteCreationParam::getName, SiteCreationParam::setName);

		formLayout.addFormItem(name, getTranslation("view.sites.add.form.name"));


		getContent().add(formLayout, buttons);
	}

	private void doSaveAction(SiteCreationParam formData, Binder<SiteCreationParam> binder) {
		binder.validate();
		if (binder.isValid()) {
			try {
				siteService.create(Site.builder()
						.name(formData.getName())
						.policyId(PolicyId.empty())
						.build());
				UI.getCurrent().navigate(SitesView.class);
			} catch (DuplicatedNameValidationError e) {
				name.setErrorMessage(getTranslation("view.sites.form.error.validation.field.name.unique"));
				name.setInvalid(true);
			} catch (RuntimeException exception) {
				LOG.error("Could not create Site. ", exception);
				showErrorNotification(getTranslation("view.sites.form.error.unexpected", "save"));
			}
		}
	}

	private void doCancelAction() {
		UI.getCurrent().navigate(SitesView.class);
	}
}
