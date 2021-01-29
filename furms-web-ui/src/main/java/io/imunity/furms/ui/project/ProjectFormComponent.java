/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.project;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.ui.components.FurmsImageUpload;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep.LabelsPosition.TOP;
import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static java.util.Optional.ofNullable;

public class ProjectFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;
	private static final int MAX_DESCRIPTION_LENGTH = 510;
	private static final int MAX_ACRONYM_LENGTH = 8;

	private final Binder<ProjectViewModel> binder;
	private final List<FurmsViewUserModel> userModels;
	private final FurmsImageUpload uploadComponent = createUploadComponent();

	public ProjectFormComponent(Binder<ProjectViewModel> binder, boolean disabe, List<FurmsViewUserModel> userModels) {
		this.binder = binder;
		this.userModels = userModels;

		FormLayout formLayout = new FormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		nameField.setEnabled(disabe);
		formLayout.addFormItem(nameField, getTranslation("view.community-admin.project.form.field.name"));

		TextArea descriptionField = new TextArea();
		descriptionField.setClassName("description-text-area");
		descriptionField.setValueChangeMode(EAGER);
		descriptionField.setMaxLength(MAX_DESCRIPTION_LENGTH);
		formLayout.addFormItem(descriptionField, getTranslation("view.community-admin.project.form.field.description"));

		TextField acronymField = new TextField();
		acronymField.setValueChangeMode(EAGER);
		acronymField.setMaxLength(MAX_ACRONYM_LENGTH);
		acronymField.setEnabled(disabe);
		formLayout.addFormItem(acronymField, getTranslation("view.community-admin.project.form.field.acronym"));

		DateTimePicker startTimePicker = new DateTimePicker();
		startTimePicker.setEnabled(disabe);
		formLayout.addFormItem(startTimePicker, getTranslation("view.community-admin.project.form.field.start-time"));

		DateTimePicker endTimePicker = new DateTimePicker();
		endTimePicker.setEnabled(disabe);
		formLayout.addFormItem(endTimePicker, getTranslation("view.community-admin.project.form.field.end-time"));

		TextField researchField = new TextField();
		researchField.setValueChangeMode(EAGER);
		researchField.setMaxLength(MAX_NAME_LENGTH);
		researchField.setEnabled(disabe);
		formLayout.addFormItem(researchField, getTranslation("view.community-admin.project.form.field.research-field"));

		ComboBox<FurmsViewUserModel> leaderComboBox = new ComboBox<>();
		leaderComboBox.setEnabled(disabe);
		leaderComboBox.setItemLabelGenerator(x -> x.firstname + " " + x.lastname + " " + x.email);
		leaderComboBox.setItems(userModels);

		formLayout.addFormItem(leaderComboBox, getTranslation("view.community-admin.project.form.field.project-leader"));

		formLayout.addFormItem(uploadComponent, getTranslation("view.community-admin.project.form.logo"));

		prepareValidator(nameField, descriptionField, acronymField, startTimePicker, endTimePicker, researchField, leaderComboBox);
		formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("1em", 1, TOP));

		getContent().add(formLayout);
	}

	private void prepareValidator(TextField nameField, TextArea descriptionField, TextField acronymField,
	                              DateTimePicker startTimePicker, DateTimePicker endTimePicker, TextField researchField,
	                              ComboBox<FurmsViewUserModel> leaderComboBox ) {
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
		binder.forField(startTimePicker)
			.withValidator(
				time -> Objects.nonNull(time) && ofNullable(endTimePicker.getValue()).map(c -> c.isAfter(time)).orElse(true),
				getTranslation("view.community-admin.project.form.error.validation.field.start-time")
			)
			.bind(ProjectViewModel::getStartTime, ProjectViewModel::setStartTime);
		binder.forField(endTimePicker)
			.withValidator(
				time -> Objects.nonNull(time) && ofNullable(startTimePicker.getValue()).map(c -> c.isBefore(time)).orElse(true),
				getTranslation("view.community-admin.project.form.error.validation.field.end-time")
			)
			.bind(ProjectViewModel::getEndTime, ProjectViewModel::setEndTime);
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
		userModels.stream()
			.filter(user -> user.id.equals(getUserId(projectViewModel)))
			.findAny()
			.ifPresent(user -> projectViewModel.projectLeader = user);
		binder.setBean(projectViewModel);
		uploadComponent.setValue(projectViewModel.getLogo());
	}

	private String getUserId(ProjectViewModel projectViewModel) {
		return ofNullable(projectViewModel.projectLeader)
			.map(user -> user.id)
			.orElse(null);
	}

	public FurmsImageUpload getUpload() {
		return uploadComponent;
	}
}
