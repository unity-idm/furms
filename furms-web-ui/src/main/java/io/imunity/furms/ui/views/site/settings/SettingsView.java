/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.settings;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
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
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsImageUpload;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
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
		FormLayout formLayout = new FormLayout();
		formLayout.setClassName("form-layout");

		Binder<SiteSettingsDto> binder = new Binder<>(SiteSettingsDto.class);
		binder.setBean(loadSite());

		formLayout.addFormItem(nameRow(binder), getTranslation("view.site-admin.settings.form.name"));
		formLayout.addFormItem(uploadRow(binder), getTranslation("view.site-admin.settings.form.logo"));
		formLayout.addFormItem(connectionInfoRow(binder), getTranslation("view.site-admin.settings.form.info"));
		formLayout.add(buttonsRow(binder));

		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("1em", 1));

		getContent().add(formLayout);
	}

	private TextField nameRow(Binder<SiteSettingsDto> binder) {
		name.setPlaceholder(getTranslation("view.site-admin.settings.form.name.placeholder"));
		name.setRequiredIndicatorVisible(true);
		name.setValueChangeMode(EAGER);
		name.setWidth("40em");
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
		textArea.setPlaceholder(getTranslation("view.site-admin.settings.form.info.placeholder"));
		binder.forField(textArea)
				.bind(SiteSettingsDto::getConnectionInfo, SiteSettingsDto::setConnectionInfo);

		return textArea;
	}

	private Component buttonsRow(Binder<SiteSettingsDto> binder) {
		Button refresh = new Button(getTranslation("view.site-admin.settings.form.button.refresh"),
				e -> doRefreshAction(binder));
		refresh.addThemeVariants(LUMO_TERTIARY);
		refresh.addClassName("sites-add-form-button");

		Button save = new Button(getTranslation("view.site-admin.settings.form.button.save"),
				e -> doSaveAction(binder));
		save.addThemeVariants(LUMO_PRIMARY);
		save.addClickShortcut(Key.ENTER);
		save.addClassName("sites-add-form-button");
		save.setEnabled(false);

		binder.addValueChangeListener(value -> {
			save.setEnabled(!binder.validate().hasErrors() && isChanged(binder.getBean()));
		});

		return new FormButtons(refresh, save);
	}

	private void doRefreshAction(Binder<SiteSettingsDto> binder) {
		refreshBinder(binder);
		showSuccessNotification(getTranslation("view.site-admin.settings.form.button.refresh.success"));
	}

	private void doSaveAction(Binder<SiteSettingsDto> binder) {
		binder.validate();
		if (binder.isValid()) {
			try {
				SiteSettingsDto settings = binder.getBean();
				siteService.update(Site.builder()
						.id(settings.getId())
						.name(settings.getName())
						.connectionInfo(settings.getConnectionInfo())
						.logo(settings.getLogo())
						.build());
				refreshBinder(binder);
				reloadRolePicker();
				showSuccessNotification(getTranslation("view.sites.form.save.success"));
			} catch (DuplicatedNameValidationError e) {
				name.setErrorMessage(getTranslation("view.site-admin.settings.form.name.validation.unique"));
				name.setInvalid(true);
			} catch (RuntimeException exception) {
				LOG.error("Error during update Site settings {}. {}",binder.getBean().getId(), exception);
				showErrorNotification(getTranslation("view.site-admin.settings.form.error.unexpected"));
			}
		}
	}

	private SiteSettingsDto loadSite() {
		try {
			Object attribute = UI.getCurrent().getSession().getAttribute(FurmsViewUserContext.class.getName());
			if (!(attribute instanceof FurmsViewUserContext)) {
				throw new IllegalArgumentException();
			}
			FurmsViewUserContext context = (FurmsViewUserContext) attribute;
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
				|| !Objects.equals(bufferedSettings.getConnectionInfo(), bean.getConnectionInfo());
	}

	private void refreshBinder(Binder<SiteSettingsDto> binder) {
		binder.setBean(null);
		binder.setBean(loadSite());
	}
}
