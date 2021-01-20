/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community_admin.projects;

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
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.WildcardParameter;
import com.vaadin.flow.server.StreamResource;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.images.FurmsImage;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.ui.config.FrontProperties;
import io.imunity.furms.ui.views.community_admin.CommunityAdminMenu;
import io.imunity.furms.ui.views.components.BreadCrumbParameter;
import io.imunity.furms.ui.views.components.FurmsViewComponent;
import io.imunity.furms.ui.views.components.PageTitle;
import io.imunity.furms.ui.views.fenix_admin.communites.CommunitiesView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Route(value = "community/admin/project/form", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.projects.page.title")
class ProjectFormView extends FurmsViewComponent {
	private final static int hundredMB = 100000000;
	private final Binder<ProjectViewModel> binder = new BeanValidationBinder<>(ProjectViewModel.class);
	private final Image image = new Image();
	private final ProjectService projectService;
	private final String[] acceptedImgFiles;

	private BreadCrumbParameter breadCrumbParameter;
	private FurmsImage logo = new FurmsImage(null, (String) null);

	ProjectFormView(ProjectService communityService, FrontProperties frontProperties) {
		this.projectService = communityService;
		this.acceptedImgFiles = frontProperties.getAcceptedImgFiles().toArray(String[]::new);

		TextField name = new TextField(getTranslation("view.community.form.field.name"));
		TextArea description = new TextArea(getTranslation("view.community.form.field.description"));
		description.setClassName("description-text-area");
		TextField acronym = new TextField(getTranslation("view.community.form.field.name"));
		DateTimePicker startTime = new DateTimePicker();
		DateTimePicker endTime = new DateTimePicker();
		TextField researchField = new TextField(getTranslation("view.community.form.field.name"));
		ComboBox<String> leader = new ComboBox<>();
		Upload upload = createUploadComponent();


		Button saveButton = createSaveButton();
		Button closeButton = createCloseButton();

		prepareValidator(name, description, acronym, startTime, endTime, researchField);

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

	private void prepareValidator(TextField name, TextArea description, TextField acronym, DateTimePicker startTime, DateTimePicker endTime, TextField researchField) {
		binder.forField(name)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank() && value.length() <= 255,
				getTranslation("view.community.form.error.validation.field.name")
			)
			.bind(ProjectViewModel::getName, ProjectViewModel::setName);
		binder.forField(description)
			.withValidator(
				value -> Objects.isNull(value) || value.length() <= 510,
				getTranslation("view.community.form.error.validation.field.description")
			)
			.bind(ProjectViewModel::getDescription, ProjectViewModel::setDescription);
		binder.forField(acronym)
			.withValidator(
				value -> Objects.nonNull(value) && value.length() <= 8,
				getTranslation("view.community.form.error.validation.field.description")
			)
			.bind(ProjectViewModel::getAcronym, ProjectViewModel::setAcronym);
		binder.forField(researchField)
			.withValidator(
				value -> Objects.nonNull(value) && value.length() <= 255,
				getTranslation("view.community.form.error.validation.field.description")
			)
			.bind(ProjectViewModel::getResearchField, ProjectViewModel::setResearchField);
		binder.forField(startTime)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.community.form.error.validation.field.description")
			)
			.bind(ProjectViewModel::getStartTime, ProjectViewModel::setStartTime);
		binder.forField(endTime)
			.withValidator(
				Objects::nonNull,
				getTranslation("view.community.form.error.validation.field.description")
			)
			.bind(ProjectViewModel::getEndTime, ProjectViewModel::setEndTime);
		binder.addStatusChangeListener(env -> binder.isValid());
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.community.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(ProjectsView.class));
		return closeButton;
	}

	private Upload createUploadComponent() {
		MemoryBuffer memoryBuffer = new MemoryBuffer();
		Upload upload = new Upload(memoryBuffer);
		upload.setAcceptedFileTypes(acceptedImgFiles);
		upload.setMaxFileSize(hundredMB);
		upload.setDropAllowed(true);
		upload.addFinishedListener(event -> {
			logo = loadFile(memoryBuffer, event.getMIMEType());
			InputStream inputStream = memoryBuffer.getInputStream();
			StreamResource streamResource = new StreamResource(event.getFileName(), () -> inputStream);
			image.setSrc(streamResource);
			image.setVisible(true);
		});
		upload.addFileRejectedListener(event ->
			Notification.show(getTranslation("view.community.form.error.validation.file"))
		);
		upload.getElement().addEventListener("file-remove", event -> {
			logo = new FurmsImage(null, (String)null);
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

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.community.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			ProjectViewModel projectViewModel = binder.getBean();
			projectViewModel.setLogo(logo);
			Project project = ProjectViewModelMapper.map(projectViewModel);
			if(project.getId() == null)
				projectService.create(project);
			else
				projectService.update(project);
			UI.getCurrent().navigate(CommunitiesView.class);
		});
		return saveButton;
	}

	private void setFormPools(ProjectViewModel projectViewModel) {
		binder.setBean(projectViewModel);
		logo = projectViewModel.getLogo();
		image.setVisible(logo != null && logo.getImage() != null);
		if(logo != null) {
			StreamResource resource =
				new StreamResource(
					UUID.randomUUID().toString() + "." + logo.getType(),
					() -> new ByteArrayInputStream(logo.getImage())
				);
			image.setSrc(resource);
		}
	}

	@Override
	public void setParameter(BeforeEvent event, @WildcardParameter String parameter) {
		ProjectViewModel projectViewModel = ofNullable(parameter)
			.map(projectService::findById)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.map(ProjectViewModelMapper::map)
			.orElseGet(ProjectViewModel::new);
		String trans = parameter == null ? "view.community.form.parameter.new" : "view.community.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		setFormPools(projectViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
