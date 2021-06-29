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
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.api.policy_documents.PolicyDocumentService;
import io.imunity.furms.domain.policy_documents.PolicyContentType;
import io.imunity.furms.domain.policy_documents.PolicyDocument;
import io.imunity.furms.domain.policy_documents.PolicyFile;
import io.imunity.furms.domain.policy_documents.PolicyId;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.PolicyFileUpload;
import io.imunity.furms.ui.views.fenix.communites.CommunitiesView;
import io.imunity.furms.ui.views.site.SiteAdminMenu;
import org.vaadin.pekka.WysiwygE;

import java.io.IOException;
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

	private BreadCrumbParameter breadCrumbParameter;

	PolicyDocumentFormView(PolicyDocumentService policyDocumentService) {
		this.policyDocumentService = policyDocumentService;
		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
//		nameField.setEnabled(restrictedEditing);
		formLayout.addFormItem(nameField, getTranslation("view.community-admin.project.form.field.name"));

		ComboBox<PolicyDocumentFormModel> workflowComboBox = new ComboBox<>();
		workflowComboBox.setItemLabelGenerator(resourceType -> resourceType.workflow.name());
		formLayout.addFormItem(workflowComboBox, getTranslation("view.community-admin.project-allocation.form.field.resource_type"));

		Label revision = new Label("revision");
		formLayout.addFormItem(revision, getTranslation("view.community-admin.project.form.field.name"));

		ComboBox<PolicyDocumentFormModel> contentTypeComboBox = new ComboBox<>();
		contentTypeComboBox.setItemLabelGenerator(resourceType -> resourceType.contentType.name());
		formLayout.addFormItem(contentTypeComboBox, getTranslation("view.community-admin.project-allocation.form.field.resource_type"));

		WysiwygE wysiwygE = new WysiwygE();
		wysiwygE.addValueChangeListener(x -> System.out.println(x.getValue()));

		PolicyFileUpload uploadComponent = createUploadComponent();

		contentTypeComboBox.addValueChangeListener(event -> {
			if(event.getValue().contentType.equals(PolicyContentType.PDF)){
				formLayout.remove(wysiwygE);
				formLayout.addFormItem(uploadComponent, getTranslation("view.community-admin.project.form.logo"));
			}
			if(event.getValue().contentType.equals(PolicyContentType.EMBEDDED)){
				formLayout.remove(uploadComponent);
				formLayout.addFormItem(wysiwygE, getTranslation("view.community-admin.project-allocation.form.field.resource_type"));
			}
		});

		FormButtons buttons = new FormButtons(createCloseButton(), createSaveButton(), createSaveWithRevisionButton());


		getContent().add(formLayout, buttons);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.fenix-admin.community.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(CommunitiesView.class));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.fenix-admin.community.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid()) {
				savePolicyDocument(false);
			}
		});
		return saveButton;
	}

	private Button createSaveWithRevisionButton() {
		Button saveButton = new Button(getTranslation("view.fenix-admin.community.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid()) {
				savePolicyDocument(true);
			}
		});
		return saveButton;
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
		} catch (Exception e) {
			showErrorNotification(getTranslation("base.error.message"));
		}
	}

	private PolicyFileUpload createUploadComponent() {
		PolicyFileUpload upload = new PolicyFileUpload();
		upload.addFinishedListener(event -> {
			try {
				binder.getBean().policyFile = upload.loadFile(event.getMIMEType());
				StreamResource streamResource =
					new StreamResource(event.getFileName(), upload.getMemoryBuffer()::getInputStream);
//				upload.getImage().setSrc(streamResource);
//				upload.getImage().setVisible(true);
			} catch (IOException e) {
				showErrorNotification(getTranslation("view.community-admin.project.form.error.validation.file"));
			}
		});
		upload.addFileRejectedListener(event ->
			showErrorNotification(getTranslation("view.community-admin.project.form.error.validation.file"))
		);
		upload.addFileRemovedListener(event -> {
			binder.getBean().policyFile = PolicyFile.empty();
//			upload.getImage().setVisible(false);
		});
		return upload;
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
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}

}
