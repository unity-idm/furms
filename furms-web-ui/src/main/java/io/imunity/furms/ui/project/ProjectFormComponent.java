/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.project;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_END_TIME;
import static io.imunity.furms.ui.views.TimeConstants.DEFAULT_START_TIME;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;

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
import io.imunity.furms.ui.components.FurmsDateTimePicker;
import io.imunity.furms.ui.components.FurmsFormLayout;
import io.imunity.furms.ui.components.FurmsImageUpload;
import io.imunity.furms.ui.components.FurmsUserComboBox;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.InvocationContext;

@CssImport("./styles/components/furms-combo-box.css")
public class ProjectFormComponent extends Composite<Div> {
	private static final int MAX_NAME_LENGTH = 20;
	private static final int MAX_DESCRIPTION_LENGTH = 510;
	private static final int MAX_ACRONYM_LENGTH = 8;

	private final Binder<ProjectViewModel> binder;
	private final List<FurmsViewUserModel> userModels;
	private final FurmsImageUpload uploadComponent = createUploadComponent();
	private ZoneId zoneId;

	public ProjectFormComponent(Binder<ProjectViewModel> binder, boolean disable, List<FurmsViewUserModel> userModels) {
		this.binder = binder;
		this.userModels = userModels;
		zoneId = InvocationContext.getCurrent().getZone();
		FormLayout formLayout = new FurmsFormLayout();

		TextField nameField = new TextField();
		nameField.setValueChangeMode(EAGER);
		nameField.setMaxLength(MAX_NAME_LENGTH);
		nameField.setEnabled(disable);
		formLayout.addFormItem(nameField, getTranslation("view.community-admin.project.form.field.name"));

		TextArea descriptionField = new TextArea();
		descriptionField.setClassName("description-text-area");
		descriptionField.setValueChangeMode(EAGER);
		descriptionField.setMaxLength(MAX_DESCRIPTION_LENGTH);
		formLayout.addFormItem(descriptionField, getTranslation("view.community-admin.project.form.field.description"));

		TextField acronymField = new TextField();
		acronymField.setValueChangeMode(EAGER);
		acronymField.setMaxLength(MAX_ACRONYM_LENGTH);
		acronymField.setEnabled(disable);
		formLayout.addFormItem(acronymField, getTranslation("view.community-admin.project.form.field.acronym"));

		FurmsDateTimePicker startDateTimePicker = new FurmsDateTimePicker(zoneId, () -> DEFAULT_START_TIME);
		formLayout.addFormItem(startDateTimePicker, getTranslation("view.community-admin.project.form.field.start-time"));

		FurmsDateTimePicker endDateTimePicker = new FurmsDateTimePicker(zoneId, () -> DEFAULT_END_TIME);
		formLayout.addFormItem(endDateTimePicker, getTranslation("view.community-admin.project.form.field.end-time"));

		TextField researchField = new TextField();
		researchField.setValueChangeMode(EAGER);
		researchField.setMaxLength(MAX_NAME_LENGTH);
		researchField.setEnabled(disable);
		formLayout.addFormItem(researchField, getTranslation("view.community-admin.project.form.field.research-field"));

		FurmsUserComboBox furmsUserComboBox = new FurmsUserComboBox(userModels);
		furmsUserComboBox.setEnabled(disable);
		furmsUserComboBox.setClassName("furms-leader-combo-box");
		formLayout.addFormItem(furmsUserComboBox, getTranslation("view.community-admin.project.form.field.project-leader"));

		formLayout.addFormItem(uploadComponent, getTranslation("view.community-admin.project.form.logo"));

		prepareValidator(nameField, descriptionField, acronymField, startDateTimePicker,
				endDateTimePicker, researchField, furmsUserComboBox);

		getContent().add(formLayout);
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
			.bind(project -> ofNullable(project.startTime)
								.map(ZonedDateTime::toLocalDateTime)
								.orElse(null),
					(project, startTime) -> project.setStartTime(startTime.atZone(zoneId)));
		binder.forField(endDateTimePicker)
			.withValidator(
					time -> Objects.nonNull(time) && ofNullable(startDateTimePicker.getValue()).map(c -> c.isBefore(time)).orElse(true),
					getTranslation("view.community-admin.project.form.error.validation.field.end-time")
			)
			.bind(project -> ofNullable(project.endTime)
								.map(ZonedDateTime::toLocalDateTime)
								.orElse(null),
				(project, endTime) -> project.setEndTime(endTime.atZone(zoneId)));
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
