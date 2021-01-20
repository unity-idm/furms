/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.config.FrontProperties;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Route(value = "community/admin/project/form", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.project.form.page.title")
class ProjectFormView extends FurmsViewComponent {
	private final static int hundredMB = 100000000;
	private final Binder<ProjectViewModel> binder = new BeanValidationBinder<>(ProjectViewModel.class);
	private final Image image = new Image();
	private final ProjectService projectService;
	private final String[] acceptedImgFiles;

	private BreadCrumbParameter breadCrumbParameter;
	private Optional<FurmsImage> logo = empty();

	ProjectFormView(ProjectService communityService, FrontProperties frontProperties) {
		this.projectService = communityService;
		this.acceptedImgFiles = frontProperties.getAcceptedImgFiles().toArray(String[]::new);

		TextField name = new TextField(getTranslation("view.community-admin.project.form.field.name"));
		TextArea description = new TextArea(getTranslation("view.community-admin.project.form.field.description"));
		description.setClassName("description-text-area");
		TextField acronym = new TextField(getTranslation("view.community-admin.project.form.field.acronym"));
		DateTimePicker startTime = new DateTimePicker(getTranslation("view.community-admin.project.form.field.start-time"));
		DateTimePicker endTime = new DateTimePicker(getTranslation("view.community-admin.project.form.field.end-time"));
		TextField researchField = new TextField(getTranslation("view.community-admin.project.form.field.research-field"));
		ComboBox<String> leader = new ComboBox<>(getTranslation("view.community-admin.project.form.field.project-leader"));
		Upload upload = createUploadComponent();

		Button saveButton = createSaveButton();
		Button closeButton = createCloseButton();

		prepareValidator(name, description, acronym, startTime, endTime, researchField, saveButton);

		VerticalLayout verticalLayout = new VerticalLayout(name, description, acronym, startTime, endTime, researchField, leader);
		verticalLayout.setClassName("no-left-padding");

		HorizontalLayout horizontalLayout = new HorizontalLayout(image, upload);
		horizontalLayout.setClassName("furms-upload-layout");
		image.setId("community-logo");

		getContent().add(
			verticalLayout,
			horizontalLayout,
			new HorizontalLayout(saveButton, closeButton)
		);
	}

	private void prepareValidator(TextField name, TextArea description, TextField acronym, DateTimePicker startTime,
	                              DateTimePicker endTime, TextField researchField, Button button) {
		binder.forField(name)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank() && value.length() <= 255,
				getTranslation("view.community-admin.project.form.error.validation.field.name")
			)
			.bind(ProjectViewModel::getName, ProjectViewModel::setName);
		binder.forField(description)
			.withValidator(
				value -> Objects.isNull(value) || value.length() <= 510,
				getTranslation("view.community-admin.project.form.error.validation.field.description")
			)
			.bind(ProjectViewModel::getDescription, ProjectViewModel::setDescription);
		binder.forField(acronym)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank() && value.length() <= 8,
				getTranslation("view.community-admin.project.form.error.validation.field.acronym")
			)
			.bind(ProjectViewModel::getAcronym, ProjectViewModel::setAcronym);
		binder.forField(researchField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank() && value.length() <= 255,
				getTranslation("view.community-admin.project.form.error.validation.field.research-field")
			)
			.bind(ProjectViewModel::getResearchField, ProjectViewModel::setResearchField);
		binder.forField(startTime)
			.withValidator(
				time -> Objects.nonNull(startTime) && ofNullable(endTime.getValue()).map(c -> c.isAfter(time)).orElse(true),
				getTranslation("view.community-admin.project.form.error.validation.field.start-time")
			)
			.bind(ProjectViewModel::getStartTime, ProjectViewModel::setStartTime);
		binder.forField(endTime)
			.withValidator(
				time -> Objects.nonNull(startTime) && ofNullable(startTime.getValue()).map(c -> c.isBefore(time)).orElse(true),
				getTranslation("view.community-admin.project.form.error.validation.field.end-time")
			)
			.bind(ProjectViewModel::getEndTime, ProjectViewModel::setEndTime);
		binder.addStatusChangeListener(env -> button.setEnabled(binder.isValid()));
	}

	private Upload createUploadComponent() {
		MemoryBuffer memoryBuffer = new MemoryBuffer();
		Upload upload = new Upload(memoryBuffer);
		upload.setAcceptedFileTypes(acceptedImgFiles);
		upload.setMaxFileSize(hundredMB);
		upload.setDropAllowed(true);
		upload.addFinishedListener(event -> {
			logo = Optional.of(loadFile(memoryBuffer, event.getMIMEType()));
			InputStream inputStream = memoryBuffer.getInputStream();
			StreamResource streamResource = new StreamResource(event.getFileName(), () -> inputStream);
			image.setSrc(streamResource);
			image.setVisible(true);
		});
		upload.addFileRejectedListener(event ->
			Notification.show(getTranslation("view.community-admin.project.form.error.validation.file"))
		);
		upload.getElement().addEventListener("file-remove", event -> {
			logo = Optional.empty();
			image.setVisible(false);
		});
		return upload;
	}

	private FurmsImage loadFile(MemoryBuffer memoryBuffer, String mimeType) {
		try {
			return new FurmsImage(memoryBuffer.getInputStream().readAllBytes(), mimeType.split("/")[1]);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.community-admin.project.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(ProjectsView.class));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.community-admin.project.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			ProjectViewModel projectViewModel = binder.getBean();
			projectViewModel.setLogo(logo.orElseGet(FurmsImage::new));
			Project project = ProjectViewModelMapper.map(projectViewModel);
			if(project.getId() == null)
				projectService.create(project);
			else
				projectService.update(project);
			UI.getCurrent().navigate(ProjectsView.class);
		});
		return saveButton;
	}

	private void setFormPools(ProjectViewModel projectViewModel) {
		binder.setBean(projectViewModel);
		logo = Optional.ofNullable(projectViewModel.getLogo());
		image.setVisible(logo.isPresent() && logo.get().getImage() != null);
		if(logo.isPresent()) {
			StreamResource resource =
				new StreamResource(
					UUID.randomUUID().toString() + "." + logo.get().getType(),
					() -> new ByteArrayInputStream(logo.get().getImage())
				);
			image.setSrc(resource);
		}
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		ProjectViewModel projectViewModel = ofNullable(parameter)
			.map(projectService::findById)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(ProjectViewModelMapper::map)
			.orElseGet(() -> new ProjectViewModel(getCurrentResourceId()));
		String trans = parameter == null ? "view.community-admin.project.form.parameter.new" : "view.community-admin.project.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		setFormPools(projectViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
