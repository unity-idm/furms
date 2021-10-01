/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.groups;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.generic_groups.GenericGroupService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.domain.generic_groups.GenericGroup;
import io.imunity.furms.domain.generic_groups.GenericGroupId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;
import io.imunity.furms.ui.views.site.policy_documents.PolicyDocumentsView;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;
import static java.util.prefs.Preferences.MAX_NAME_LENGTH;

@Route(value = "community/admin/groups/form", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.groups.form.page.title")
public class GroupFormView extends FurmsViewComponent {
	private static final int MAX_DESCRIPTION_LENGTH = 510;

	private final GenericGroupService genericGroupService;

	private final Binder<GroupFormModel> binder = new BeanValidationBinder<>(GroupFormModel.class);
	private final Div buttonLayout = new Div();
	private final ComboBox<PolicyWorkflow> workflowComboBox = new ComboBox<>();
	private final String communityId;

	private BreadCrumbParameter breadCrumbParameter;

	GroupFormView(GenericGroupService genericGroupService) {
		this.genericGroupService = genericGroupService;
		this.communityId = getCurrentResourceId();
		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.community-admin.groups.form.layout.name"));

		TextArea descriptionField = new TextArea();
		descriptionField.setClassName("description-text-area");
		descriptionField.setValueChangeMode(EAGER);
		descriptionField.setMaxLength(MAX_DESCRIPTION_LENGTH);
		formLayout.addFormItem(descriptionField, getTranslation("view.community-admin.groups.form.layout.description"));

		prepareValidator(nameField, descriptionField);

		getContent().add(formLayout, buttonLayout);
	}

	private void addCreateButtons() {
		FormButtons buttons = new FormButtons(
			createCloseButton(),
			createSaveButton(getTranslation("view.community-admin.groups.form.button.save"), false)
		);
		buttonLayout.removeAll();
		buttonLayout.add(buttons);
	}

	private void addUpdateButtons() {
		FormButtons buttons = new FormButtons(
			createCloseButton(),
			createSaveButton(getTranslation("view.community-admin.groups.form.button.save"), true)
		);
		buttonLayout.removeAll();
		buttonLayout.add(buttons);
	}

	private void prepareValidator(TextField nameField, TextArea descriptionField) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.community-admin.groups.form.error.validation.name")
			)
			.bind(model -> model.name, (model, name) -> model.name = name);

		binder.forField(descriptionField)
			.bind(model -> model.description, (model, description) -> model.description = description);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.community-admin.groups.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(GroupsView.class));
		return closeButton;
	}

	private Button createSaveButton(String text, boolean update) {
		Button saveButton = new Button(text);
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid()) {
				try {
					GroupFormModel bean = binder.getBean();
					GenericGroup genericGroup = GenericGroup.builder()
						.id(bean.id)
						.communityId(bean.communityId)
						.name(bean.name)
						.description(bean.description)
						.build();
					if (update)
						genericGroupService.update(genericGroup);
					else
						genericGroupService.create(genericGroup);

					UI.getCurrent().navigate(GroupsView.class);
				} catch (DuplicatedNameValidationError e) {
					showErrorNotification(getTranslation("name.duplicated.error.message"));
				} catch (Exception e) {
					showErrorNotification(getTranslation("base.error.message"));
					throw e;
				}
			}
		});
		return saveButton;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		GroupFormModel groupFormModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> genericGroupService.findBy(communityId, new GenericGroupId(id))))
			.flatMap(Function.identity())
			.map(x -> new GroupFormModel(x.id, x.communityId, x.name, x.description))
			.orElseGet(() -> new GroupFormModel(communityId));

		String trans = parameter == null
			? "view.site-admin.policy-documents.form.parameter.new"
			: "view.site-admin.policy-documents.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		binder.setBean(groupFormModel);
		if(groupFormModel.id.id == null)
			addCreateButtons();
		else {
			addUpdateButtons();
			workflowComboBox.setReadOnly(true);
		}
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

}
