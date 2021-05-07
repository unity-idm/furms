/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.community_allocation.CommunityAllocationService;
import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.resource_allocations.ResourceAllocationsGrid;
import io.imunity.furms.ui.components.resource_allocations.ResourceAllocationsGridItem;
import io.imunity.furms.ui.views.community.projects.allocations.ProjectAllocationDashboardFormView;
import io.imunity.furms.ui.views.fenix.dashboard.DashboardGridResource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaadin.flow.component.checkbox.CheckboxGroupVariant.LUMO_VERTICAL;
import static io.imunity.furms.domain.constant.RoutesConst.COMMUNITY_BASE_LANDING_PAGE;
import static io.imunity.furms.ui.utils.ResourceGetter.getCurrentResourceId;
import static io.imunity.furms.ui.views.community.DashboardViewFilters.Checkboxes.Options.INCLUDED_EXPIRED;
import static io.imunity.furms.ui.views.community.DashboardViewFilters.Checkboxes.Options.INCLUDED_FULLY_DISTRIBUTED;
import static java.util.Optional.ofNullable;

@Route(value = COMMUNITY_BASE_LANDING_PAGE, layout = CommunityAdminMenu.class)
@PageTitle(key = "view.community-admin.dashboard.page.title")
public class DashboardView extends FurmsViewComponent {

	private final CommunityAllocationService allocationService;

	private final DashboardViewFilters filters;
	private final ResourceAllocationsGrid grid;

	DashboardView(CommunityAllocationService allocationService) {
		this.allocationService = allocationService;

		this.filters = new DashboardViewFilters();
		this.grid = new ResourceAllocationsGrid(this::allocateButtonAction, this::loadCredits);

		addTitle();
		addFilters();
		getContent().add(grid);
	}

	private void addTitle() {
		final ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.community-admin.dashboard.title"));
		getContent().add(headerLayout);
	}

	private void addFilters() {
		final CheckboxGroup<DashboardViewFilters.Checkboxes> checkboxGroup = new CheckboxGroup<>();
		checkboxGroup.setItemLabelGenerator(DashboardViewFilters.Checkboxes::getLabel);
		checkboxGroup.setItems(
				new DashboardViewFilters.Checkboxes(INCLUDED_FULLY_DISTRIBUTED,
						getTranslation("view.community-admin.dashboard.filters.fully-distributed")),
				new DashboardViewFilters.Checkboxes(INCLUDED_EXPIRED,
						getTranslation("view.community-admin.dashboard.filters.expired")));
		checkboxGroup.addThemeVariants(LUMO_VERTICAL);
		checkboxGroup.addValueChangeListener(event -> {
			filters.setIncludedFullyDistributed(isSelectedCheckbox(INCLUDED_FULLY_DISTRIBUTED, event.getValue()));
			filters.setIncludedExpired(isSelectedCheckbox(INCLUDED_EXPIRED, event.getValue()));
			UI.getCurrent().accessSynchronously(grid::reloadGrid);
		});

		getContent().add(checkboxGroup);
	}

	private boolean isSelectedCheckbox(DashboardViewFilters.Checkboxes.Options checkbox,
	                                   Set<DashboardViewFilters.Checkboxes> value) {
		return value.stream()
				.anyMatch(filter -> checkbox.equals(filter.getOption()));
	}

	private void allocateButtonAction(ResourceAllocationsGridItem item) {
		ComponentUtil.setData(UI.getCurrent(), ResourceAllocationsGridItem.class, item);
		UI.getCurrent().navigate(ProjectAllocationDashboardFormView.class);
	}

	private Stream<ResourceAllocationsGridItem> loadCredits() {
		return allocationService.findAllWithRelatedObjects(
					getCurrentResourceId(),
					filters.isIncludedFullyDistributed(),
					filters.isIncludedExpired())
				.stream()
				.map(this::buildItem);
	}

	private ResourceAllocationsGridItem buildItem(CommunityAllocationResolved communityAllocation) {
		return ResourceAllocationsGridItem.builder()
				.id(communityAllocation.id)
				.siteId(communityAllocation.site.getId())
				.siteName(communityAllocation.site.getName())
				.communityId(communityAllocation.communityId)
				.name(communityAllocation.name)
				.split(communityAllocation.resourceCredit.split)
				.resourceTypeId(communityAllocation.resourceType.id)
				.credit(createResource(communityAllocation.amount, communityAllocation.resourceType.unit))
				.consumed(createResource(calcConsumed(communityAllocation), communityAllocation.resourceType.unit))
				.remaining(createResource(communityAllocation.remaining, communityAllocation.resourceType.unit))
				.created(extractLocalDate(communityAllocation.resourceCredit.utcCreateTime))
				.validFrom(extractLocalDate(communityAllocation.resourceCredit.utcStartTime))
				.validTo(extractLocalDate(communityAllocation.resourceCredit.utcEndTime))
				.build();
	}

	private BigDecimal calcConsumed(CommunityAllocationResolved credit) {
		if (credit == null || credit.amount == null || credit.remaining == null) {
			return BigDecimal.ZERO;
		}
		return credit.amount.subtract(credit.remaining);
	}

	private DashboardGridResource createResource(BigDecimal amount, ResourceMeasureUnit unit) {
		return DashboardGridResource.builder()
				.amount(amount)
				.unit(unit)
				.build();
	}

	private LocalDate extractLocalDate(LocalDateTime dateTime) {
		return ofNullable(dateTime)
				.map(LocalDateTime::toLocalDate)
				.orElse(null);
	}

}
