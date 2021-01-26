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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
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
import io.imunity.furms.ui.components.FurmsImageUpload;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.config.FrontProperties;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static com.vaadin.flow.data.value.ValueChangeMode.EAGER;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "community/admin/project/form", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.project.form.page.title")
class ProjectFormView extends FurmsViewComponent {
	private final Binder<ProjectViewModel> binder = new BeanValidationBinder<>(ProjectViewModel.class);
	private final ProjectService projectService;
	private final FurmsImageUpload upload;

	private BreadCrumbParameter breadCrumbParameter;

	ProjectFormView(ProjectService communityService, FrontProperties frontProperties) {
		this.projectService = communityService;
		this.upload = createUploadComponent();

		TextField name = new TextField(getTranslation("view.community-admin.project.form.field.name"));
		name.setValueChangeMode(EAGER);
		TextArea description = new TextArea(getTranslation("view.community-admin.project.form.field.description"));
		description.setClassName("description-text-area");
		description.setValueChangeMode(EAGER);
		TextField acronym = new TextField(getTranslation("view.community-admin.project.form.field.acronym"));
		acronym.setValueChangeMode(EAGER);
		DateTimePicker startTime = new DateTimePicker(getTranslation("view.community-admin.project.form.field.start-time"));
		DateTimePicker endTime = new DateTimePicker(getTranslation("view.community-admin.project.form.field.end-time"));
		TextField researchField = new TextField(getTranslation("view.community-admin.project.form.field.research-field"));
		researchField.setValueChangeMode(EAGER);
		ComboBox<String> leader = new ComboBox<>(getTranslation("view.community-admin.project.form.field.project-leader"));

		Button saveButton = createSaveButton();
		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));
		Button closeButton = createCloseButton();

		prepareValidator(name, description, acronym, startTime, endTime, researchField);

		VerticalLayout verticalLayout = new VerticalLayout(name, description, acronym, startTime, endTime, researchField, leader);
		verticalLayout.setClassName("no-left-padding");

		getContent().add(
			verticalLayout,
			upload,
			new HorizontalLayout(saveButton, closeButton)
		);
	}

	private void prepareValidator(TextField name, TextArea description, TextField acronym, DateTimePicker startTime,
	                              DateTimePicker endTime, TextField researchField) {
		binder.forField(name)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.community-admin.project.form.error.validation.field.name.1")
			)
			.withValidator(
				value -> value.length() <= 20,
				getTranslation("view.community-admin.project.form.error.validation.field.name.2")
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
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.community-admin.project.form.error.validation.field.acronym.1")
			)
			.withValidator(
				value -> value.length() <= 8,
				getTranslation("view.community-admin.project.form.error.validation.field.acronym.2")
			)
			.bind(ProjectViewModel::getAcronym, ProjectViewModel::setAcronym);
		binder.forField(researchField)
			.withValidator(
				value -> Objects.nonNull(value) && !value.isBlank(),
				getTranslation("view.community-admin.project.form.error.validation.field.research-field.1")
			)
			.withValidator(
				value -> value.length() <= 20,
				getTranslation("view.community-admin.project.form.error.validation.field.research-field.2")
			)
			.bind(ProjectViewModel::getResearchField, ProjectViewModel::setResearchField);
		binder.forField(startTime)
			.withValidator(
				time -> Objects.nonNull(time) && ofNullable(endTime.getValue()).map(c -> c.isAfter(time)).orElse(true),
				getTranslation("view.community-admin.project.form.error.validation.field.start-time")
			)
			.bind(ProjectViewModel::getStartTime, ProjectViewModel::setStartTime);
		binder.forField(endTime)
			.withValidator(
				time -> Objects.nonNull(time) && ofNullable(startTime.getValue()).map(c -> c.isBefore(time)).orElse(true),
				getTranslation("view.community-admin.project.form.error.validation.field.end-time")
			)
			.bind(ProjectViewModel::getEndTime, ProjectViewModel::setEndTime);
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
		upload.addFileRemovedListener( event -> {
			binder.getBean().setLogo(FurmsImage.empty());
			upload.getImage().setVisible(false);
		});
		return upload;
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
			binder.validate();
			if(binder.isValid())
				saveProject();
		});
		return saveButton;
	}

	private void saveProject() {
		ProjectViewModel projectViewModel = binder.getBean();
		Project project = ProjectViewModelMapper.map(projectViewModel);
		OptionalException<Void> optionalException;
		if(project.getId() == null)
			optionalException = getResultOrException(() -> projectService.create(project));
		else
			optionalException = getResultOrException(() -> projectService.update(project));

		optionalException.getThrowable().ifPresentOrElse(
			throwable -> NotificationUtils.showErrorNotification(getTranslation("project.error.message")),
			() -> UI.getCurrent().navigate(ProjectsView.class)
		);
	}

	private void setFormPools(ProjectViewModel projectViewModel) {
		binder.setBean(projectViewModel);
		upload.setValue(projectViewModel.getLogo());
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		ProjectViewModel projectViewModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> projectService.findById(id)))
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
