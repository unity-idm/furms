/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.spi.users.UsersDAO;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.project.ProjectFormComponent;
import io.imunity.furms.ui.project.ProjectViewModel;
import io.imunity.furms.ui.project.ProjectViewModelMapper;
import io.imunity.furms.ui.user_context.FurmsViewUserModel;
import io.imunity.furms.ui.user_context.FurmsViewUserModelMapper;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.util.List;
import java.util.Optional;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "community/admin/project/form", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.project.form.page.title")
class ProjectFormView extends FurmsViewComponent {
	private final Binder<ProjectViewModel> binder = new BeanValidationBinder<>(ProjectViewModel.class);
	private final ProjectFormComponent projectFormComponent;
	private final ProjectService projectService;

	private BreadCrumbParameter breadCrumbParameter;

	ProjectFormView(ProjectService projectService, UsersDAO usersDAO) {
		this.projectService = projectService;
		List<FurmsViewUserModel> users = FurmsViewUserModelMapper.mapList(usersDAO.getAllUsers());
		this.projectFormComponent = new ProjectFormComponent(binder, true, users);
		Button saveButton = createSaveButton();
		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));
		Button closeButton = createCloseButton();

		getContent().add(
			projectFormComponent,
			new HorizontalLayout(saveButton, closeButton)
		);
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
		projectFormComponent.setFormPools(projectViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
