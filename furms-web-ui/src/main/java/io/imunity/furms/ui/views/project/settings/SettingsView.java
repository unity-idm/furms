/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.settings;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.users.UserService;
import io.imunity.furms.domain.projects.ProjectAdminControlledAttributes;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.project.ProjectFormComponent;
import io.imunity.furms.ui.project.ProjectModelResolver;
import io.imunity.furms.ui.project.ProjectViewModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;
import io.imunity.furms.ui.user_context.UIContext;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static io.imunity.furms.ui.components.FurmsLayout.callReloadLogo;
import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.NotificationUtils.showSuccessNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.function.Function.identity;

@Route(value = "project/admin/settings", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.settings.page.title")
public class SettingsView extends FurmsViewComponent {
	private final Binder<ProjectViewModel> binder = new BeanValidationBinder<>(ProjectViewModel.class);
	private final Button updateButton = createUpdateButton();
	private final Button cancelButton = createCloseButton();
	private final ProjectFormComponent projectFormComponent;
	private final ProjectService projectService;
	private final ProjectModelResolver resolver;
	private ZoneId zoneId;

	private ProjectViewModel oldProject;

	SettingsView(ProjectService projectService, UserService userService, ProjectModelResolver resolver) {
		this.projectService = projectService;
		this.resolver = resolver;
		List<FurmsViewUserModel> users = FurmsViewUserModelMapper.mapList(userService.getAllUsers());
		this.projectFormComponent = new ProjectFormComponent(binder, true, users);
		zoneId = UIContext.getCurrent().getZone();

		projectFormComponent.getUpload().addFinishedListener(x -> enableEditorMode());
		projectFormComponent.getUpload().addFileRemovedListener(x -> enableEditorMode());
		Optional<ProjectViewModel> projectViewModel = getProjectViewModel();
		if(projectViewModel.isPresent()){
			oldProject = projectViewModel.get();
			projectFormComponent.setFormPools(new ProjectViewModel(oldProject));
			disableEditorMode();
		}
		if(!projectService.isProjectInTerminalState(oldProject.id)){
			projectFormComponent.readOnlyAll();
		}

		binder.addStatusChangeListener(status -> {
			if(oldProject.equalsFields(binder.getBean()))
				disableEditorMode();
			else
				enableEditorMode();
		});


		FormButtons buttons = new FormButtons(cancelButton, updateButton);
		getContent().add(
			projectFormComponent, buttons
		);
	}

	private void enableEditorMode() {
		updateButton.setVisible(true);
		updateButton.setEnabled(binder.isValid());
		cancelButton.setVisible(true);
	}

	private void disableEditorMode() {
		cancelButton.setVisible(false);
		updateButton.setVisible(false);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.project-admin.settings.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickListener(event ->{
			loadProject();
			closeButton.setVisible(false);
			projectFormComponent.getUpload().cleanCurrentFileName();
		});
		return closeButton;
	}

	private void loadProject() {
		getProjectViewModel()
			.ifPresent(projectFormComponent::setFormPools);
	}

	private Optional<ProjectViewModel> getProjectViewModel() {
		return handleExceptions(() -> projectService.findById(getCurrentResourceId()))
			.flatMap(identity())
			.map(project -> resolver.resolve(project, zoneId));
	}

	private Button createUpdateButton() {
		Button updateButton = new Button(getTranslation("view.project-admin.settings.button.update"));
		updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		updateButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid()) {
				ProjectViewModel projectViewModel = new ProjectViewModel(binder.getBean());
				ProjectAdminControlledAttributes project = new ProjectAdminControlledAttributes(
						projectViewModel.id,
						projectViewModel.description,
						projectViewModel.researchField,
						projectViewModel.logo);
				getResultOrException(() -> projectService.update(project))
					.getException()
					.ifPresentOrElse(
						e -> showErrorNotification(getTranslation("base.error.message")),
						() -> {
							if (isLogoChange(oldProject, projectViewModel)) {
								callReloadLogo(this.getClass());
							}
							oldProject = projectViewModel;
							disableEditorMode();
							showSuccessNotification(getTranslation("view.project-admin.settings.update.success"));
						}
					);
			}
		});
		return updateButton;
	}

	private boolean isLogoChange(ProjectViewModel oldProject, ProjectViewModel projectViewModel) {
		return !Arrays.equals(oldProject.getLogo().getImage(), projectViewModel.getLogo().getImage());
	}
}
