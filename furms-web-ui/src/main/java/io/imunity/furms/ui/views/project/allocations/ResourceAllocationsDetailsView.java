/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.allocations;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.views.project.ProjectAdminMenu;

import java.util.Optional;
import java.util.function.Function;

import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "project/admin/resource/allocations/details", layout = ProjectAdminMenu.class)
@PageTitle(key = "view.project-admin.resource-allocations.details.page.title")
public class ResourceAllocationsDetailsView extends FurmsViewComponent {
	private final ProjectAllocationService projectAllocationService;
	private BreadCrumbParameter breadCrumbParameter;

	ResourceAllocationsDetailsView(ProjectAllocationService projectAllocationService) {
		this.projectAllocationService = projectAllocationService;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {

		String projectAllocationName = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> projectAllocationService.findByProjectIdAndId(getCurrentResourceId(), id)))
			.flatMap(Function.identity())
			.map(allocation -> allocation.name)
			.orElse(null);

		breadCrumbParameter = new BreadCrumbParameter(parameter, projectAllocationName);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
