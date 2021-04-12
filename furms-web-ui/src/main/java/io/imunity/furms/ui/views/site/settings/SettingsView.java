/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.settings;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.components.*;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.views.site.PolicyDocumentsView;
import io.imunity.furms.ui.views.site.SiteAdminMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY;
import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.FormSettings.NAME_MAX_LENGTH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;

@Route(value = "site/admin/settings", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.settings.page.title")
@CssImport("./styles/views/site/settings/site-settings.css")
public class SettingsView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final static String NAME_FIELD_ID = "name";

	private final SiteService siteService;

	private final TextField name;

	private SiteSettingsDto bufferedSettings;

	SettingsView(SiteService siteService) {
		this.siteService = siteService;
		this.name = new TextField();

		addForm();
	}

	private void addForm() {
		FormLayout formLayout = new FurmsFormLayout();

		Binder<SiteSettingsDto> binder = new Binder<>(SiteSettingsDto.class);
		binder.setBean(loadSite());

		formLayout.addFormItem(nameRow(binder), getTranslation("view.site-admin.settings.form.name"));
		formLayout.addFormItem(connectionInfoRow(binder), getTranslation("view.site-admin.settings.form.info"));
		formLayout.addFormItem(sshKeyFromMandatory(binder), "");
		formLayout.addFormItem(uploadRow(binder), getTranslation("view.site-admin.settings.form.logo"));
		formLayout.add(buttonsRow(binder));

		getContent().add(formLayout);
	}

	private TextField nameRow(Binder<SiteSettingsDto> binder) {
		name.setPlaceholder(getTranslation("view.site-admin.settings.form.name.placeholder"));
		name.setRequiredIndicatorVisible(true);
		name.setValueChangeMode(EAGER);
		name.setMaxLength(NAME_MAX_LENGTH);
		name.setId(NAME_FIELD_ID);

		binder.forField(name)
				.withValidator(getNotEmptyStringValidator(),
						getTranslation("view.site-admin.settings.form.name.validation.required"))
				.bind(SiteSettingsDto::getName, SiteSettingsDto::setName);

		return name;
	}

	private FurmsImageUpload uploadRow(Binder<SiteSettingsDto> binder) {
		FurmsImageUpload upload = new FurmsImageUpload();

		upload.addFinishedListener(event -> {
			try {
				binder.getBean().setLogo(upload.loadFile(event.getMIMEType()));
				StreamResource streamResource = new StreamResource(event.getFileName(), upload.getMemoryBuffer()::getInputStream);
				upload.getImage().setSrc(streamResource);
				upload.getImage().setVisible(true);
			} catch (IOException e) {
				showErrorNotification(getTranslation("view.site-admin.settings.form.logo.error"));
				LOG.error("Could not load Image", e);
			}
		});

		upload.addFileRejectedListener(event -> showErrorNotification(event.getErrorMessage()));

		upload.addFileRemovedListener(event -> {
			binder.getBean().setLogo(FurmsImage.empty());
			upload.getImage().setVisible(false);
		});

		binder.forField(upload)
				.bind(SiteSettingsDto::getLogo, SiteSettingsDto::setLogo);

		return upload;
	}

	private Component connectionInfoRow(Binder<SiteSettingsDto> binder) {
		TextArea textArea = new TextArea();
		textArea.setValueChangeMode(EAGER);
		textArea.setClassName("large-description-text-area");
		textArea.setPlaceholder(getTranslation("view.site-admin.settings.form.info.placeholder"));
		binder.forField(textArea)
				.bind(SiteSettingsDto::getConnectionInfo, SiteSettingsDto::setConnectionInfo);

		return textArea;
	}
	
	private Checkbox sshKeyFromMandatory(Binder<SiteSettingsDto> binder) {
		Checkbox sshKeyFromMandatoryCheckbox = new Checkbox(getTranslation("view.site-admin.settings.form.sshKeyFromOptionMandatory"));
		binder.forField(sshKeyFromMandatoryCheckbox).bind(SiteSettingsDto::isSshKeyFromOptionMandatory, SiteSettingsDto::setSshKeyFromOptionMandatory);
		return sshKeyFromMandatoryCheckbox;
	}

	private Component buttonsRow(Binder<SiteSettingsDto> binder) {
		
		Button cancel = new Button(getTranslation("view.site-admin.settings.form.button.cancel"));
		cancel.addThemeVariants(LUMO_TERTIARY);
		Button save = new Button(getTranslation("view.site-admin.settings.form.button.save"));
		save.addThemeVariants(LUMO_PRIMARY);
		FormButtons formButtons = new FormButtons(cancel, save);
		formButtons.setVisible(false);
		
		cancel.addClickListener(event -> {
			refreshBinder(binder);
			formButtons.setVisible(false);
		});
		save.addClickListener(event -> {
			doSaveAction(binder, formButtons);
		});

		binder.addValueChangeListener(value -> {
			if (isChanged(binder.getBean())) {
				formButtons.setVisible(true);
				formButtons.setEnabled(binder.isValid());
			}
			else {
				formButtons.setVisible(false);
			}
		});

		return formButtons;
	}

	private void doSaveAction(Binder<SiteSettingsDto> binder, FormButtons formButtons) {
		binder.validate();
		if (binder.isValid()) {
			try {
				SiteSettingsDto settings = binder.getBean();
				siteService.update(Site.builder()
						.id(settings.getId())
						.name(settings.getName())
						.connectionInfo(settings.getConnectionInfo())
						.logo(settings.getLogo())
						.sshKeyFromOptionMandatory(settings.isSshKeyFromOptionMandatory())
						.build());
				refreshBinder(binder);
				showSuccessNotification(getTranslation("view.sites.form.save.success"));
				formButtons.setVisible(false);
			} catch (DuplicatedNameValidationError e) {
				name.setErrorMessage(getTranslation("view.site-admin.settings.form.name.validation.unique"));
				name.setInvalid(true);
			} catch (RuntimeException e) {
				LOG.error("Error during update Site settings.", e);
				showErrorNotification(getTranslation("view.site-admin.settings.form.error.unexpected"));
			}
		}
	}

	private SiteSettingsDto loadSite() {
		try {
			FurmsViewUserContext context = getActualViewUserContext();
			bufferedSettings = siteService.findById(context.id)
					.map(SiteSettingsDto::new)
					.orElseThrow(IllegalArgumentException::new);
			return bufferedSettings.clone();
		} catch (IllegalArgumentException e) {
			UI.getCurrent().navigate(PolicyDocumentsView.class);
			showErrorNotification(getTranslation("view.site-admin.settings.page.error.not-found"));
			return null;
		}
	}

	private boolean isChanged(SiteSettingsDto bean) {
		return !Objects.equals(bufferedSettings.getName(), bean.getName())
				|| !Objects.equals(bufferedSettings.getLogo(), bean.getLogo())
				|| !Objects.equals(bufferedSettings.getConnectionInfo(), bean.getConnectionInfo())
				|| !Objects.equals(bufferedSettings.isSshKeyFromOptionMandatory(), bean.isSshKeyFromOptionMandatory());
	}

	private void refreshBinder(Binder<SiteSettingsDto> binder) {
		binder.setBean(loadSite());
	}
}
