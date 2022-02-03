/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.project.allocations;

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.export.ResourceUsageCSVExporter;
import io.imunity.furms.api.export.ResourceUsageJSONExporter;
import io.imunity.furms.api.project_allocation.ProjectAllocationService;
import io.imunity.furms.domain.project_allocation.ProjectAllocation;
import io.imunity.furms.ui.charts.service.ChartPowerService;
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
	private final ResourceUsageJSONExporter jsonExporter;
	private final ResourceUsageCSVExporter csvExporter;
	private final String projectId;
	private BreadCrumbParameter breadCrumbParameter;

	ResourceAllocationsDetailsView(ProjectAllocationService projectAllocationService,
	                               ChartPowerService chartPowerService, ResourceUsageJSONExporter jsonExporter,
	                               ResourceUsageCSVExporter csvExporter) {
		this.projectAllocationService = projectAllocationService;
		this.chartPowerService = chartPowerService;
		this.jsonExporter = jsonExporter;
		this.csvExporter = csvExporter;
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

			ToggleButton toggle = new ToggleButton("Show per-user breakdown");
			toggle.addValueChangeListener(evt -> {
				getContent().removeAll();
				if(evt.getValue())
					getContent().add(toggle, getResourceAllocationChartWithUsersUsage(projectAllocation.get()));
				else
					getContent().add(toggle, getBasicResourceAllocationChart(projectAllocation.get()));

			});
			getContent().add(toggle, getBasicResourceAllocationChart(projectAllocation.get()));
		}
	}

	private ResourceAllocationChart getBasicResourceAllocationChart(ProjectAllocation projectAllocation) {
		return new ResourceAllocationChart(
			chartPowerService.getChartDataForProjectAlloc(projectAllocation.projectId, projectAllocation.id),
			jsonExporter.getJsonForProjectAllocation(projectAllocation.projectId, projectAllocation.id),
			csvExporter.getCsvForProjectAllocation(projectAllocation.projectId, projectAllocation.id)
		);
	}

	private ResourceAllocationChart getResourceAllocationChartWithUsersUsage(ProjectAllocation projectAllocation) {
		return new ResourceAllocationChart(
			chartPowerService.getChartDataForProjectAllocWithUserUsages(projectAllocation.projectId, projectAllocation.id),
			jsonExporter.getJsonForProjectAllocation(projectAllocation.projectId, projectAllocation.id),
			csvExporter.getCsvForProjectAllocation(projectAllocation.projectId, projectAllocation.id)
		);
	}

	@Override
	public Optional<BreadCrumbParameter> getParameter() {
		return Optional.ofNullable(breadCrumbParameter);
	}
}
