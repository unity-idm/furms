/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.project;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.users.PersistentId;
import io.imunity.furms.ui.components.FormTextArea;
import io.imunity.furms.ui.components.FormTextField;
import io.imunity.furms.ui.components.FurmsDateTimePicker;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsImageUpload;
import io.imunity.furms.ui.components.FurmsUserComboBox;
import io.imunity.furms.ui.components.IdFormItem;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.api.constant.ValidationConst.MAX_ACRONYM_LENGTH;
import static io.imunity.furms.api.constant.ValidationConst.MAX_DESCRIPTION_LENGTH;
import static io.imunity.furms.api.constant.ValidationConst.MAX_PROJECT_NAME_LENGTH;
import static io.imunity.furms.api.constant.ValidationConst.MAX_RESEARCH_NAME_LENGTH;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_END_TIME;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_START_TIME;
import static java.util.Optional.ofNullable;

@CssImport("./styles/components/furms-combo-box.css")
public class ProjectFormComponent extends Composite<Div> {
	private final Binder<ProjectViewModel> binder;
	private final List<FurmsViewUserModel> userModels;
	private final FurmsImageUpload uploadComponent = createUploadComponent();
	private final TextArea descriptionField = new FormTextArea();
	private final FurmsDateTimePicker startDateTimePicker;
	private final FurmsDateTimePicker endDateTimePicker;
	private final IdFormItem idFormItem;

	public ProjectFormComponent(Binder<ProjectViewModel> binder, boolean restrictedEditing, List<FurmsViewUserModel> userModels) {
		this.binder = binder;
		this.userModels = userModels;

		final FormLayout formLayout = new FurmsFormLayout();

		idFormItem = new IdFormItem(getTranslation("view.community-admin.project.form.field.furms-id"));
		idFormItem.setVisible(false);
		formLayout.add(idFormItem);

		TextField nameField = new FormTextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_PROJECT_NAME_LENGTH);
		nameField.setReadOnly(restrictedEditing);
		nameField.setClassName("long-default-name-field");
		formLayout.addFormItem(nameField, getTranslation("view.community-admin.project.form.field.name"));

		descriptionField.setClassName("description-text-area");
		descriptionField.setValueChangeMode(EAGER);
		descriptionField.setMaxLength(MAX_DESCRIPTION_LENGTH);
		formLayout.addFormItem(descriptionField, getTranslation("view.community-admin.project.form.field.description"));

		TextField acronymField = new FormTextField();
		acronymField.setValueChangeMode(EAGER);
		acronymField.setMaxLength(MAX_ACRONYM_LENGTH);
		acronymField.setReadOnly(restrictedEditing);
		formLayout.addFormItem(acronymField, getTranslation("view.community-admin.project.form.field.acronym"));

		startDateTimePicker = new FurmsDateTimePicker(() -> DEFAULT_START_TIME);
		startDateTimePicker.setReadOnly(restrictedEditing);
		formLayout.addFormItem(startDateTimePicker, getTranslation("view.community-admin.project.form.field.start-time"));

		endDateTimePicker = new FurmsDateTimePicker(() -> DEFAULT_END_TIME);
		endDateTimePicker.setReadOnly(restrictedEditing);
		formLayout.addFormItem(endDateTimePicker, getTranslation("view.community-admin.project.form.field.end-time"));

		TextField researchField = new FormTextField();
		researchField.setValueChangeMode(EAGER);
		researchField.setMaxLength(MAX_RESEARCH_NAME_LENGTH);
		formLayout.addFormItem(researchField, getTranslation("view.community-admin.project.form.field.research-field"));

		FurmsUserComboBox furmsUserComboBox = new FurmsUserComboBox(userModels, true);
		furmsUserComboBox.setReadOnly(restrictedEditing);
		furmsUserComboBox.setClassName("furms-leader-combo-box");
		formLayout.addFormItem(furmsUserComboBox, getTranslation("view.community-admin.project.form.field.project-leader"));

		formLayout.addFormItem(uploadComponent, getTranslation("view.community-admin.project.form.logo"));

		prepareValidator(nameField, descriptionField, acronymField, startDateTimePicker,
				endDateTimePicker, researchField, furmsUserComboBox);

		getContent().add(formLayout);
	}

	public void readOnlyAll(){
		descriptionField.setReadOnly(true);
		startDateTimePicker.setReadOnly(true);
		endDateTimePicker.setReadOnly(true);
	}

	private void prepareValidator(TextField nameField, TextArea descriptionField, TextField acronymField,
	                              FurmsDateTimePicker startDateTimePicker, FurmsDateTimePicker endDateTimePicker,
	                              TextField researchField, FurmsUserComboBox leaderComboBox ) {
		binder.forField(nameField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.community-admin.project.form.error.validation.field.name")
			)
			.bind(ProjectViewModel::getName, ProjectViewModel::setName);
		binder.forField(descriptionField)
			.bind(ProjectViewModel::getDescription, ProjectViewModel::setDescription);
		binder.forField(acronymField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.community-admin.project.form.error.validation.field.acronym")
			)
			.bind(ProjectViewModel::getAcronym, ProjectViewModel::setAcronym);
		binder.forField(researchField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.community-admin.project.form.error.validation.field.research-field")
			)
			.bind(ProjectViewModel::getResearchField, ProjectViewModel::setResearchField);
		binder.forField(startDateTimePicker)
			.withValidator(
				time -> Objects.nonNull(time) && ofNullable(endDateTimePicker.getValue()).map(c -> c.isAfter(time)).orElse(true),
				getTranslation("view.community-admin.project.form.error.validation.field.start-time")
			)
			.bind(project -> ofNullable(project.startTime).orElse(null),
				ProjectViewModel::setStartTime);
		binder.forField(endDateTimePicker)
			.withValidator(
					time -> Objects.nonNull(time) && ofNullable(startDateTimePicker.getValue()).map(c -> c.isBefore(time)).orElse(true),
					getTranslation("view.community-admin.project.form.error.validation.field.end-time")
			)
			.bind(project -> ofNullable(project.endTime).orElse(null),
				ProjectViewModel::setEndTime);
		binder.forField(leaderComboBox)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.community-admin.project.form.error.validation.field.leader")
			)
			.bind(ProjectViewModel::getProjectLeader, ProjectViewModel::setProjectLeader);
	}

	private FurmsImageUpload createUploadComponent() {
		FurmsImageUpload upload = new FurmsImageUpload();
		upload.addFinishedListener(event -> {
			try {
				binder.getBean().setLogo(upload.loadFile(event.getMIMEType()));
				StreamResource streamResource =
					new StreamResource(event.getFileName(), upload.getMemoryBuffer()::getInputStream);
				upload.getImage().setSrc(streamResource);
				upload.getImage().setVisible(true);
			} catch (IOException e) {
				showErrorNotification(getTranslation("view.community-admin.project.form.error.validation.file"));
			}
		});
		upload.addFileRejectedListener(event ->
			showErrorNotification(getTranslation("view.community-admin.project.form.error.validation.file"))
		);
		upload.addFileRemovedListener(event -> {
			binder.getBean().setLogo(FurmsImage.empty());
			upload.getImage().setVisible(false);
		});
		return upload;
	}

	public void setFormPools(ProjectViewModel projectViewModel) {
		final PersistentId userId = getUserId(projectViewModel);
		userModels.stream()
			.filter(user -> user.id.isPresent() && user.id.get().equals(userId))
			.findAny()
			.ifPresent(user -> projectViewModel.projectLeader = user);
		binder.setBean(projectViewModel);
		idFormItem.setIdAndShow(ofNullable(projectViewModel.id)
			.flatMap(id -> ofNullable(id.id))
			.map(UUID::toString)
			.orElse(null)
		);
		uploadComponent.setValue(projectViewModel.getLogo());
	}

	private PersistentId getUserId(ProjectViewModel projectViewModel) {
		return ofNullable(projectViewModel.projectLeader)
			.flatMap(user -> user.id)
			.orElse(null);
	}

	public FurmsImageUpload getUpload() {
		return uploadComponent;
	}
}
