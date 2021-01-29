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
import io.imunity.furms.domain.projects.LimitedProject;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.project.ProjectFormComponent;
import io.imunity.furms.ui.project.ProjectViewModel;
import io.imunity.furms.ui.project.ProjectViewModelMapper;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.util.List;
import java.util.Optional;

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
	private final Button closeButton = createCloseButton();
	private final ProjectFormComponent projectFormComponent;
	private final ProjectService projectService;

	private ProjectViewModel oldProject;

	SettingsView(ProjectService projectService, UsersDAO usersDAO) {
		this.projectService = projectService;
		List<FurmsViewUserModel> users = FurmsViewUserModelMapper.mapList(usersDAO.getAllUsers());
		this.projectFormComponent = new ProjectFormComponent(binder, false, users);

		projectFormComponent.getUpload().addFinishedListener(x -> enableEditorMode());
		projectFormComponent.getUpload().addFileRemovedListener(x -> enableEditorMode());
		Optional<ProjectViewModel> projectViewModel = getProjectViewModel();
		if(projectViewModel.isPresent()){
			oldProject = projectViewModel.get();
			projectFormComponent.setFormPools(new ProjectViewModel(oldProject));
			disableEditorMode();
		}

		binder.addStatusChangeListener(status -> {
			if(oldProject.equalsFields(binder.getBean()))
				disableEditorMode();
			else
				enableEditorMode();
		});

		getContent().add(
			projectFormComponent,
			updateButton, closeButton
		);
	}

	private void enableEditorMode() {
		updateButton.setEnabled(binder.isValid());
		closeButton.setVisible(true);
	}

	private void disableEditorMode() {
		closeButton.setVisible(false);
		updateButton.setEnabled(false);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.project-admin.settings.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickListener(event ->{
			loadProject();
			closeButton.setVisible(false);
			projectFormComponent.getUpload().clean();
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
			.map(ProjectViewModelMapper::map);
	}

	private Button createUpdateButton() {
		Button updateButton = new Button(getTranslation("view.project-admin.settings.button.update"));
		updateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		updateButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid()) {
				ProjectViewModel projectViewModel = new ProjectViewModel(binder.getBean());
				LimitedProject project = new LimitedProject(projectViewModel.id, projectViewModel.description, projectViewModel.logo);
				getResultOrException(() -> projectService.limitedUpdate(project))
					.getThrowable()
					.ifPresentOrElse(
						e -> showErrorNotification(getTranslation("project.error.message")),
						() -> {
							oldProject = projectViewModel;
							disableEditorMode();
							showSuccessNotification(getTranslation("view.project-admin.settings.update.success"));
						}
					);
			}
		});
		return updateButton;
	}
}
