/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

import java.lang.invoke.MethodHandles;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import io.imunity.furms.api.authz.AuthzService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.api.ssh_keys.SSHKeyHistoryException;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.api.validation.exceptions.UserWithoutFenixIdValidationError;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.user_settings.UserSettingsMenu;

@Route(value = "users/settings/ssh/keys/form", layout = UserSettingsMenu.class)
@PageTitle(key = "view.user-settings.ssh-keys.form.page.title")
class SSHKeyFormView extends FurmsViewComponent {

	private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Binder<SSHKeyUpdateModel> binder = new BeanValidationBinder<>(SSHKeyUpdateModel.class);
	private final SSHKeyFormComponent sshKeyComponent;
	private final SSHKeyService sshKeyService;
	private final AuthzService authzService;
	private BreadCrumbParameter breadCrumbParameter;
	private ZoneId zoneId;
	private SiteComboBoxModelResolver resolver;
	
	SSHKeyFormView(SSHKeyService sshKeysService, SiteService siteService, AuthzService authzService) {

		this.sshKeyService = sshKeysService;
		this.authzService = authzService;
		this.resolver = new SiteComboBoxModelResolver(siteService.findAll());
		this.sshKeyComponent = new SSHKeyFormComponent(binder, resolver, sshKeysService);
		UI.getCurrent().getPage().retrieveExtendedClientDetails(extendedClientDetails -> {
			zoneId = ZoneId.of(extendedClientDetails.getTimeZoneId());
		});

		Button saveButton = createSaveButton();
		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));
		Button cancelButton = createCloseButton();

		FormButtons buttons = new FormButtons(cancelButton, saveButton);
		getContent().add(sshKeyComponent, buttons);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.user-settings.ssh-keys.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(SSHKeysView.class));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.user-settings.ssh-keys.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if (binder.isValid())
				saveSSHKey();
		});
		return saveButton;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {

		SSHKeyUpdateModel serviceViewModel = ofNullable(parameter)
				.flatMap(id -> handleExceptions(() -> sshKeyService.findById(id)))
				.flatMap(Function.identity()).map(k -> SSHKeyViewModelMapper.mapToUpdate(k, zoneId))
				.orElseGet(() -> new SSHKeyUpdateModel(authzService.getCurrentUserId()));

		String trans = parameter == null ? "view.user-settings.ssh-keys.form.parameter.new"
				: "view.user-settings.ssh-keys.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		sshKeyComponent.setFormPools(serviceViewModel);
	}

	private void saveSSHKey() {
		SSHKeyUpdateModel sshKeyUpdateModel = binder.getBean();

		try {
			if (sshKeyUpdateModel.id == null) {
				sshKeyService.create(SSHKeyViewModelMapper.map(sshKeyUpdateModel));
			} else {
				sshKeyUpdateModel.setUpdateTime(ZonedDateTime.now());
				sshKeyService.update(SSHKeyViewModelMapper.map(sshKeyUpdateModel));
			}
		} catch (UserWithoutFenixIdValidationError e) {
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("user.without.fenixid.error.message"));
			return;
		} catch (SSHKeyHistoryException e) {
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("view.user-settings.ssh-keys.history.error.message", resolver.getName(e.siteId)));
			return;	
		}
		catch (Exception e) {
			LOG.error(e.getMessage(), e);
			showErrorNotification(getTranslation("base.error.message"));
			return;
		}

		UI.getCurrent().navigate(SSHKeysView.class);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
