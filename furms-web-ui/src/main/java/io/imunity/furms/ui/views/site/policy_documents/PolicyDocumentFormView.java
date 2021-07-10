/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.policy_documents;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.PolicyDocumentIsInconsistentException;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyFile;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.domain.policy_documents.PolicyWorkflow;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsDialog;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.PolicyFileUpload;
import io.imunity.furms.ui.views.site.SiteAdminMenu;
import org.vaadin.pekka.WysiwygE;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;
import static java.util.prefs.Preferences.MAX_NAME_LENGTH;

@Route(value = "site/admin/policy/documents/form", layout = SiteAdminMenu.class)
@PageTitle(key = "view.site-admin.policy-documents.page.title")
public class PolicyDocumentFormView extends FurmsViewComponent {
	private final PolicyDocumentService policyDocumentService;

	private final Binder<PolicyDocumentFormModel> binder = new BeanValidationBinder<>(PolicyDocumentFormModel.class);
	private final Label revision = new Label();
	private final ComboBox<PolicyWorkflow> workflowComboBox = new ComboBox<>();
	;

	private BreadCrumbParameter breadCrumbParameter;

	PolicyDocumentFormView(PolicyDocumentService policyDocumentService) {
		this.policyDocumentService = policyDocumentService;
		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		formLayout.addFormItem(nameField, getTranslation("view.site-admin.policy-documents.form.layout.name"));

		workflowComboBox.setItems(Arrays.stream(PolicyWorkflow.values()));
		workflowComboBox.setItemLabelGenerator(workflow -> getTranslation("view.site-admin.policy-documents.form.layout.workflow." + workflow.getPersistentId()));
		formLayout.addFormItem(workflowComboBox, getTranslation("view.site-admin.policy-documents.form.layout.workflow"));

		formLayout.addFormItem(revision, getTranslation("view.site-admin.policy-documents.form.layout.revision"));

		ComboBox<PolicyContentType> contentTypeComboBox = new ComboBox<>();
		contentTypeComboBox.setItems(Arrays.stream(PolicyContentType.values()));
		contentTypeComboBox.setItemLabelGenerator(contentType -> getTranslation("view.site-admin.policy-documents.form.layout.content-type." + contentType.getPersistentId()));
		formLayout.addFormItem(contentTypeComboBox, getTranslation("view.site-admin.policy-documents.form.layout.content-type"));

		WysiwygE wysiwygE = new WysiwygE();

		PolicyFileUpload uploadComponent = new PolicyFileUpload();

		FormLayout.FormItem formItem = formLayout.addFormItem(new Div(), "");

		contentTypeComboBox.addValueChangeListener(event -> {
			if(event.getValue().equals(PolicyContentType.PDF)){
				formItem.removeAll();
				wysiwygE.clear();
				formItem.add(uploadComponent);
			}
			if(event.getValue().equals(PolicyContentType.EMBEDDED)){
				formItem.removeAll();
				uploadComponent.clear();
				formItem.add(wysiwygE);
			}
		});

		prepareValidator(nameField, workflowComboBox, contentTypeComboBox, wysiwygE, uploadComponent);

		getContent().add(formLayout);
	}

	private void addCreateButtons() {
		FormButtons buttons = new FormButtons(
			createCloseButton(),
			createSaveButton(getTranslation("view.site-admin.policy-documents.form.button.save"), false)
		);
		getContent().add(buttons);
	}

	private void addUpdateButtons() {
		FormButtons buttons = new FormButtons(
			createCloseButton(),
			createSaveButton(getTranslation("view.site-admin.policy-documents.form.button.save"), false),
			createSaveButton(getTranslation("view.site-admin.policy-documents.form.button.save-with-revision"), true)
		);
		getContent().add(buttons);
	}

	private void prepareValidator(TextField nameField,
	                              ComboBox<PolicyWorkflow> workflowComboBox,
	                              ComboBox<PolicyContentType> contentTypeComboBox,
	                              WysiwygE wysiwygE, PolicyFileUpload uploadComponent) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.site-admin.policy-documents.form.error.validation.name")
			)
			.bind(model -> model.name, (model, name) -> model.name = name);
		binder.forField(workflowComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.site-admin.policy-documents.form.error.validation.workflow")
			)
			.bind(model -> model.workflow, (model, workflow) -> model.workflow = workflow);
		binder.forField(contentTypeComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.site-admin.policy-documents.form.error.validation.content-type")
			)
			.bind(model -> model.contentType, (model, contentType) -> model.contentType = contentType);
		binder.forField(wysiwygE)
			.withValidator(
				obj -> Objects.nonNull(obj) || !uploadComponent.isEmpty(),
				getTranslation("view.site-admin.policy-documents.form.error.validation.text")
			)
			.bind(model -> model.wysiwygText, (model, wysiwygText) -> model.wysiwygText = wysiwygText);
		binder.forField(uploadComponent)
			.bind(model -> model.policyFile, (model, policyFile) -> model.policyFile = policyFile != null ? policyFile : PolicyFile.empty());
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.fenix-admin.community.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(PolicyDocumentsView.class));
		return closeButton;
	}

	private Button createSaveButton(String text, boolean withRevision) {
		Button saveButton = new Button(text);
		Dialog confirmDialog = createConfirmDialog();
		saveButton.getStyle().set("margin-right", "0.5em");
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid()) {
				if(withRevision)
					confirmDialog.open();
				else
					savePolicyDocument(false);
			}
		});
		return saveButton;
	}

	private Dialog createConfirmDialog() {
		FurmsDialog furmsDialog = new FurmsDialog(getTranslation("view.site-admin.policy-documents.confirm.dialog"));
		furmsDialog.addConfirmButtonClickListener(event -> {
			savePolicyDocument(true);
		});
		return furmsDialog;
	}

	private void savePolicyDocument(boolean withRevision) {
		PolicyDocumentFormModel policyDocumentFormModel = binder.getBean();
		PolicyDocument policyDocument = PolicyDocumentFormModelMapper.map(policyDocumentFormModel);
		try {
			if (policyDocument.id == null)
				policyDocumentService.create(policyDocument);
			else if (withRevision)
				policyDocumentService.updateWithRevision(policyDocument);
			else
				policyDocumentService.update(policyDocument);
			UI.getCurrent().navigate(PolicyDocumentsView.class);
		} catch (DuplicatedNameValidationError e) {
			showErrorNotification(getTranslation("name.duplicated.error.message"));
		} catch (PolicyDocumentIsInconsistentException e) {
			showErrorNotification(getTranslation("policy.document.terminal-state.message"));
		} catch (Exception e) {
			showErrorNotification(getTranslation("base.error.message"));
			throw e;
		}
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		PolicyDocumentFormModel policyDocumentFormModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> policyDocumentService.findById(getCurrentResourceId(), new PolicyId(id))))
			.flatMap(Function.identity())
			.map(PolicyDocumentFormModelMapper::map)
			.orElseGet(() -> new PolicyDocumentFormModel(getCurrentResourceId()));

		String trans = parameter == null
			? "view.site-admin.policy-documents.form.parameter.new"
			: "view.site-admin.policy-documents.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		binder.setBean(policyDocumentFormModel);
		revision.setText(String.valueOf(policyDocumentFormModel.revision + 1));
		if(policyDocumentFormModel.id == null)
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
