/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.export.ResourceUsageCSVExporter;
import io.imunity.furms.api.export.ResourceUsageJSONExporter;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.domain.project_allocation.ProjectAllocationId;
import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.ui.charts.service.ChartPoweringService;
import io.imunity.furms.ui.charts.ResourceAllocationChart;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.views.community.CommunityAdminMenu;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "community/admin/project/allocations/details", layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.project.resource-allocations.details.page.title")
public class ProjectAllocationsDetailsView extends FurmsViewComponent {
	private final ProjectAllocationService projectAllocationService;
	private final ChartPoweringService chartPoweringService;
	private final ResourceUsageJSONExporter jsonExporter;
	private final ResourceUsageCSVExporter csvExporter;
	private BreadCrumbParameter breadCrumbParameter;

	ProjectAllocationsDetailsView(ProjectAllocationService projectAllocationService, ChartPoweringService chartPoweringService,
	                              ResourceUsageJSONExporter jsonExporter, ResourceUsageCSVExporter csvExporter) {
		this.projectAllocationService = projectAllocationService;
		this.chartPoweringService = chartPoweringService;
		this.jsonExporter = jsonExporter;
		this.csvExporter = csvExporter;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		Optional<ProjectId> projectId = event.getLocation()
			.getQueryParameters()
			.getParameters()
			.getOrDefault("projectId", List.of())
			.stream().findAny()
			.map(ProjectId::new);

		Optional<ProjectAllocation> projectAllocation = ofNullable(parameter)
			.filter(id -> projectId.isPresent())
			.map(ProjectAllocationId::new)
			.flatMap(id -> handleExceptions(() -> projectAllocationService.findByProjectIdAndId(projectId.get(), id)))
			.flatMap(Function.identity());

		if(projectAllocation.isPresent()) {
			getContent().removeAll();
			breadCrumbParameter = new BreadCrumbParameter(
				parameter,
				projectAllocation.get().name,
				getTranslation("view.user-settings.projects.page.details.bread-crumb")
			);

			ResourceAllocationChart resourceAllocationChart = new ResourceAllocationChart(
				chartPoweringService.getChartDataForProjectAlloc(projectAllocation.get().projectId, projectAllocation.get().id),
				jsonExporter.getJsonForProjectAllocation(projectAllocation.get().projectId, projectAllocation.get().id),
				csvExporter.getCsvForProjectAllocation(projectAllocation.get().projectId, projectAllocation.get().id),
				true
			);
			getContent().add(resourceAllocationChart);
		}
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
