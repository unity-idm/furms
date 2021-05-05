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
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.ui.components.BreadCrumbParameter;
import io.imunity.furms.ui.components.FormButtons;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.utils.NotificationUtils;
import io.imunity.furms.ui.utils.OptionalException;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;
import io.imunity.furms.ui.views.community.projects.ProjectView;

import java.util.Optional;
import java.util.function.Function;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.getResultOrException;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;

@Route(value = "community/admin/project/allocation/form", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.project-allocation.form.page.title")
class ProjectAllocationFormView extends FurmsViewComponent {
	private final Binder<ProjectAllocationViewModel> binder = new BeanValidationBinder<>(ProjectAllocationViewModel.class);
	private final ProjectAllocationFormComponent projectAllocationFormComponent;
	private final ProjectAllocationService projectAllocationService;
	private final ProjectAllocationComboBoxesModelsResolver resolver;
	private final String communityId;

	private String projectId;

	private BreadCrumbParameter breadCrumbParameter;

	ProjectAllocationFormView(ProjectAllocationService projectAllocationService) {
		this.projectAllocationService = projectAllocationService;
		this.resolver = new ProjectAllocationComboBoxesModelsResolver(
			projectAllocationService.findCorrelatedCommunityAllocation(getCurrentResourceId()),
			projectAllocationService::getAvailableAmount
		);
		this.projectAllocationFormComponent = new ProjectAllocationFormComponent(binder, resolver);
		this.communityId = getCurrentResourceId();

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
		closeButton.addClickListener(x -> UI.getCurrent().navigate(ProjectView.class, projectId));
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
		OptionalException<Void> optionalException;
		if(projectAllocation.id == null)
			optionalException = getResultOrException(() -> projectAllocationService.create(communityId, projectAllocation));
		else
			optionalException = getResultOrException(() -> projectAllocationService.update(communityId, projectAllocation));

		optionalException.getThrowable().ifPresentOrElse(
			throwable -> NotificationUtils.showErrorNotification(getTranslation(throwable.getMessage())),
			() -> UI.getCurrent().navigate(ProjectView.class, projectId)
		);
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		ProjectAllocationViewModel serviceViewModel = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> projectAllocationService.findByIdWithRelatedObjects(communityId, id)))
			.flatMap(Function.identity())
			.map(ProjectAllocationModelsMapper::map)
			.orElseGet(ProjectAllocationViewModel::new);

		String projectId = event.getLocation()
			.getQueryParameters()
			.getParameters()
			.getOrDefault("projectId", singletonList(serviceViewModel.getProjectId()))
			.iterator().next();
		Optional.ofNullable(projectId)
			.ifPresent(id -> this.projectId = id);
		serviceViewModel.setProjectId(this.projectId);

		String trans = parameter == null
			? "view.community-admin.project-allocation.form.parameter.new"
			: "view.community-admin.project-allocation.form.parameter.update";
		breadCrumbParameter = new BreadCrumbParameter(parameter, getTranslation(trans));
		projectAllocationFormComponent.setFormPools(serviceViewModel);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
