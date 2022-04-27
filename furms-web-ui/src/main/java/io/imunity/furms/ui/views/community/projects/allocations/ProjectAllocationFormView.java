/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.api.projects.ProjectService;
import io.imunity.furms.api.validation.exceptions.DuplicatedNameValidationError;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationDecreaseBeyondUsageException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationIncreaseInExpiredProjectException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationIsNotInTerminalStateException;
import io.imunity.furms.api.validation.exceptions.ProjectAllocationWrongAmountException;
import io.imunity.furms.api.validation.exceptions.ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException;
import io.imunity.furms.domain.communities.CommunityId;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.Project;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;
import io.imunity.furms.ui.views.community.projects.ProjectView;

import java.util.Optional;
import java.util.function.Function;

import static io.imunity.furms.ui.utils.NotificationUtils.showErrorNotification;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "community/admin/project/allocation/form", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.project-allocation.form.page.title")
class ProjectAllocationFormView extends FurmsViewComponent {

	private final ProjectAllocationService projectAllocationService;
	private final ProjectService projectService;

	private final Binder<ProjectAllocationViewModel> binder = new BeanValidationBinder<>(ProjectAllocationViewModel.class);
	private final ProjectAllocationFormComponent projectAllocationFormComponent;

	private final CommunityId communityId;
	private ProjectId projectId;
	private BreadCrumbParameter breadCrumbParameter;

	ProjectAllocationFormView(ProjectAllocationService projectAllocationService,
	                          CommunityAllocationService communityAllocationService,
	                          ProjectService projectService) {
		this.projectAllocationService = projectAllocationService;
		this.projectService = projectService;
		this.communityId = new CommunityId(getCurrentResourceId());
		ProjectAllocationComboBoxesModelsResolver resolver = new ProjectAllocationComboBoxesModelsResolver(
			communityAllocationService.findAllNotExpiredByCommunityIdWithRelatedObjects(communityId),
			projectAllocationService::getAvailableAmount
		);
		this.projectAllocationFormComponent = new ProjectAllocationFormComponent(binder, resolver);

		Button saveButton = createSaveButton();
		binder.addStatusChangeListener(status -> saveButton.setEnabled(binder.isValid()));
		Button cancelButton = createCloseButton();

		FormButtons buttons = new FormButtons(cancelButton, saveButton);
		getContent().add(projectAllocationFormComponent, buttons);
	}

	private Button createCloseButton() {
		Button closeButton = new Button(getTranslation("view.community-admin.project-allocation.form.button.cancel"));
		closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		closeButton.addClickShortcut(Key.ESCAPE);
		closeButton.addClickListener(x -> UI.getCurrent().navigate(ProjectView.class, projectId.id.toString()));
		return closeButton;
	}

	private Button createSaveButton() {
		Button saveButton = new Button(getTranslation("view.community-admin.project-allocation.form.button.save"));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveButton.addClickListener(x -> {
			binder.validate();
			if(binder.isValid())
				saveProjectAllocation();
		});
		return saveButton;
	}

	private void saveProjectAllocation() {
		ProjectAllocationViewModel allocationViewModel = binder.getBean();
		ProjectAllocation projectAllocation = ProjectAllocationModelsMapper.map(allocationViewModel);
		try {
			if(projectAllocation.id == null)
				projectAllocationService.create(communityId, projectAllocation);
			else
				projectAllocationService.update(communityId, projectAllocation);

			UI.getCurrent().navigate(ProjectView.class, projectId.id.toString());
		} catch (ProjectHasMoreThenOneResourceTypeAllocationInGivenTimeException e) {
			showErrorNotification(getTranslation("project.allocation.resource.type.unique.message"));
		} catch (ProjectAllocationWrongAmountException e) {
			showErrorNotification(getTranslation("project.allocation.wrong.amount.message"));
		} catch (ProjectAllocationIncreaseInExpiredProjectException e) {
			showErrorNotification(getTranslation("project.allocation.increase.amount.in.expired.project.message"));
		} catch (ProjectAllocationDecreaseBeyondUsageException e) {
			showErrorNotification(getTranslation("project.allocation.decrease.amount.beyond.usage.message"));
		} catch (ProjectAllocationIsNotInTerminalStateException e) {
			showErrorNotification(getTranslation("project.allocation.terminal-state.message"));
		} catch (DuplicatedNameValidationError e) {
			if(projectAllocationFormComponent.isNameDefault()) {
				showErrorNotification(getTranslation("default.name.duplicated.error.message"));
				projectAllocationFormComponent.reloadDefaultName();
			}
			else
				showErrorNotification(getTranslation("name.duplicated.error.message"));
		} catch (Exception e) {
			showErrorNotification(getTranslation("base.error.message"));
		}

	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		ProjectAllocationViewModel projectAllocationModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> projectAllocationService.findByIdWithRelatedObjects(communityId,
			new ProjectAllocationId(id))))
			.flatMap(Function.identity())
			.map(ProjectAllocationModelsMapper::map)
			.orElseGet(() -> {
				ProjectId projectId = new ProjectId(event.getLocation()
					.getQueryParameters()
					.getParameters()
					.get("projectId")
					.iterator().next());
				Project project = projectService.findById(projectId).get();
				return new ProjectAllocationViewModel(projectId, project.getName());
			});
		this.projectId = projectAllocationModel.getProjectId();
		String trans = parameter == null
			? "view.community-admin.project-allocation.form.parameter.new"
			: "view.community-admin.project-allocation.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		projectAllocationFormComponent.setFormPools(projectAllocationModel, () -> projectAllocationService.getOccupiedNames(communityId, projectId));
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
