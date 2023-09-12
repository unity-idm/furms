/*
 * Copyright (c) 2021 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */

package io.imunity.furms.ui.views.user_settings.ssh_keys;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import io.imunity.furms.api.ssh_keys.SSHKeyService;
import io.imunity.furms.domain.ssh_keys.InvalidSSHKeyFromOptionException;
import io.imunity.furms.domain.ssh_keys.SSHKey;
import io.imunity.furms.domain.ssh_keys.SSHKeyFromOptionValidator;
import io.imunity.furms.ui.components.BigMultiSelectComboBox;
import io.imunity.furms.ui.components.FormTextField;
import io.imunity.furms.ui.components.FurmsFormLayout;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.api.constant.ValidationConst.MAX_SSH_KEY_NAME_LENGTH;


@CssImport("./styles/views/settings/ssh-keys.css")
@CssImport(value = "./styles/components/textarea-error-message.css", themeFor = "vaadin-text-area")
class SSHKeyFormComponent extends Composite<Div> {

	private final Binder<SSHKeyUpdateModel> binder;
	private final SiteComboBoxModelResolver resolver;
	private final SSHKeyService sshKeyService;
	private final TextArea keyValueField;
	private final MultiSelectComboBox<SiteComboBoxModel> sitesComboBox;
	
	SSHKeyFormComponent(Binder<SSHKeyUpdateModel> binder, SiteComboBoxModelResolver resolver,
			SSHKeyService keyService) {
		this.binder = binder;
		this.resolver = resolver;
		this.sshKeyService = keyService;

		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new FormTextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_SSH_KEY_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.user-settings.ssh-keys.form.field.name"));

		sitesComboBox = new BigMultiSelectComboBox<>();
		sitesComboBox.setItems(resolver.getSites());
		sitesComboBox.setItemLabelGenerator(site -> site.name);
		formLayout.addFormItem(sitesComboBox,
				getTranslation("view.user-settings.ssh-keys.form.combo-box.sites"));

		keyValueField = new TextArea();
		keyValueField.setValueChangeMode(EAGER);
		keyValueField.setClassName("wide-text-area");
		formLayout.addFormItem(keyValueField, getTranslation("view.user-settings.ssh-keys.form.field.key"));
		
		prepareValidator(nameField, sitesComboBox, keyValueField);

		formLayout.setSizeFull();
		getContent().add(formLayout);
	}

	private void prepareValidator(TextField nameField, MultiSelectComboBox<SiteComboBoxModel> sitesComboBox,
			TextArea keyValueField) {
		binder.forField(nameField)
				.withValidator(value -> Objects.nonNull(value) && !value.isBlank(), getTranslation(
						"view.user-settings.ssh-keys.form.error.validation.field.name"))
				.withValidator(value -> (binder.getBean().id == null
						? !sshKeyService.isNamePresent(value)
						: !sshKeyService.isNamePresentIgnoringRecord(value,
								binder.getBean().id)),
						getTranslation("view.user-settings.ssh-keys.form.error.validation.field.name.unique"))
				.bind(SSHKeyUpdateModel::getName, SSHKeyUpdateModel::setName);

		binder.forField(keyValueField)
				.withValidator(value -> Objects.nonNull(value) && !value.isBlank(), getTranslation(
						"view.user-settings.ssh-keys.form.error.validation.field.key"))
				.withValidator(new SSHKeyValueValidator())
				.bind(SSHKeyUpdateModel::getValue, SSHKeyUpdateModel::setValue);
		keyValueField.addValueChangeListener(e -> binder.validate());

		binder.forField(sitesComboBox).withValidator(new SiteValidator()).bind(
				sshKeyViewModel -> sshKeyViewModel.getSites().stream().map(resolver::getSite)
						.collect(Collectors.toSet()),
				(sshKeyViewModel, sites) -> sshKeyViewModel
						.setSites(sites.stream().map(s -> s.id).collect(Collectors.toSet())));
	}

	private boolean validateKey(String value) {

		try {
			SSHKey.validate(value);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public void setFormPools(SSHKeyUpdateModel sshKeyViewModel) {
		binder.setBean(sshKeyViewModel);
		if (sshKeyViewModel.getName() != null) {
			binder.validate();
		}
	}

	public class SSHKeyValueValidator implements Validator<String> {
		@Override
		public ValidationResult apply(String value, ValueContext context) {
			if(value != null && value.contains("\n"))
				return ValidationResult.error(getTranslation("view.user-settings.ssh-keys.form.error.validation.field.key.invalid.format.newLine"));
			try {
				Map<String, String> keyOptions = SSHKey.getKeyOptions(value);

				if (sitesComboBox.getValue().stream().anyMatch(s -> s.sshKeyFromOptionMandatory)) {
					SSHKeyFromOptionValidator.validateFromOption(keyOptions.get("from"));
				}

			} catch (InvalidSSHKeyFromOptionException e) {
				return ValidationResult.error(getTranslation(
						"view.user-settings.ssh-keys.form.error.validation.field.key.invalid.from.option", getTranslation("view.user-settings.ssh-keys.form.error.validation.field.key.invalid.from.option.cause." + e.type.toString())));

			} catch (Exception e) {
				return ValidationResult.error(getTranslation(
						"view.user-settings.ssh-keys.form.error.validation.field.key.invalid.format"));
			}
			return ValidationResult.ok();
		}
	}
	
	public class SiteValidator implements Validator<Set<SiteComboBoxModel>> {

		@Override
		public ValidationResult apply(Set<SiteComboBoxModel> value, ValueContext context) {
			if (value == null || value.isEmpty())
				return ValidationResult.error(getTranslation("view.user-settings.ssh-keys.form.error.validation.field.sites"));
			
			String keyValue = keyValueField.getValue();
			if(keyValue == null
					|| keyValue.isEmpty() || !validateKey(keyValue))
				return ValidationResult.ok();

			if (!SSHKey.getKeyOptions(keyValue).containsKey("from")) {
				Set<SiteComboBoxModel> siteWithMandatoryFromOption = value.stream()
						.filter(s -> s.sshKeyFromOptionMandatory).collect(Collectors.toSet());
				if (!siteWithMandatoryFromOption.isEmpty()) {
					return ValidationResult.error(getTranslation(
							"view.user-settings.ssh-keys.form.combo-box.invalid.sites",
							siteWithMandatoryFromOption.stream().map(s -> s.name)
									.collect(Collectors.joining(", "))));
				}
			}
			return ValidationResult.ok();
		}
	}
}
