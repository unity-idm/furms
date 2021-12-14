/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.allocations;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.ui.charts.ChartPowerService;
import io.imunity.furms.ui.charts.ResourceAllocationChart;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
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
	private final ChartPowerService chartPowerService;
	private final String projectId;
	private BreadCrumbParameter breadCrumbParameter;

	ResourceAllocationsDetailsView(ProjectAllocationService projectAllocationService, ChartPowerService chartPowerService) {
		this.projectAllocationService = projectAllocationService;
		this.chartPowerService = chartPowerService;
		this.projectId = getCurrentResourceId();
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		Optional<ProjectAllocation> projectAllocation = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> projectAllocationService.findByProjectIdAndId(projectId, id)))
			.flatMap(Function.identity());

		if(projectAllocation.isPresent()) {
			getContent().removeAll();
			breadCrumbParameter = new BreadCrumbParameter(
				parameter,
				projectAllocation.get().name,
				getTranslation("view.project-admin.resource-allocations.details.page.bread-crumb")
			);

			ResourceAllocationChart resourceAllocationChart = new ResourceAllocationChart(
				chartPowerService.getChartData(projectAllocation.get().projectId, projectAllocation.get().id),
				chartPowerService.getJsonFile(projectAllocation.get().projectId, projectAllocation.get().id),
				chartPowerService.getCsvFile(projectAllocation.get().projectId, projectAllocation.get().id)
			);
			getContent().add(resourceAllocationChart);
		}
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
