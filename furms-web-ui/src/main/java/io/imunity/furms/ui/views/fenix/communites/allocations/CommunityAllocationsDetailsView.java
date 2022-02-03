/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.communites.allocations;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.api.export.ResourceUsageCSVExporter;
import io.imunity.furms.api.export.ResourceUsageJSONExporter;
import io.imunity.furms.domain.community_allocation.CommunityAllocation;
import io.imunity.furms.ui.charts.service.ChartPowerService;
import io.imunity.furms.ui.charts.ResourceAllocationChart;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.layout.BreadCrumbParameter;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.util.Optional;
import java.util.function.Function;

import static io.imunity.furms.ui.utils.VaadinExceptionHandler.handleExceptions;
import static java.util.Optional.ofNullable;

@Route(value = "fenix/admin/community/allocations/details", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.community.resource-allocations.details.page.title")
public class CommunityAllocationsDetailsView extends FurmsViewComponent {
	private final CommunityAllocationService communityAllocationService;
	private final ChartPowerService chartPowerService;
	private final ResourceUsageJSONExporter jsonExporter;
	private final ResourceUsageCSVExporter csvExporter;
	private BreadCrumbParameter breadCrumbParameter;

	CommunityAllocationsDetailsView(CommunityAllocationService communityAllocationService,
	                                ChartPowerService chartPowerService, ResourceUsageJSONExporter jsonExporter,
	                                ResourceUsageCSVExporter csvExporter) {
		this.communityAllocationService = communityAllocationService;
		this.chartPowerService = chartPowerService;
		this.jsonExporter = jsonExporter;
		this.csvExporter = csvExporter;
	}

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		Optional<CommunityAllocation> communityAllocation = ofNullable(parameter)
			.flatMap(id -> handleExceptions(() -> communityAllocationService.findById(id)))
			.flatMap(Function.identity());

		if(communityAllocation.isPresent()) {
			getContent().removeAll();
			breadCrumbParameter = new BreadCrumbParameter(
				parameter,
				communityAllocation.get().name,
				getTranslation("view.fenix-admin.community.resource-allocations.details.page.bread-crumb")
			);

			ResourceAllocationChart resourceAllocationChart = new ResourceAllocationChart(
				chartPowerService.getChartDataForCommunityAlloc(communityAllocation.get().communityId, communityAllocation.get().id),
				jsonExporter.getJsonForCommunityAllocation(communityAllocation.get().communityId, communityAllocation.get().id),
				csvExporter.getCsvForCommunityAllocation(communityAllocation.get().communityId, communityAllocation.get().id),
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
