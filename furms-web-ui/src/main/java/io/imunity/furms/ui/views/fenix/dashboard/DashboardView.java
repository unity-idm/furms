/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.dashboard;

import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.resource_credits.ResourceCreditWithAllocations;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.components.resource_allocations.ResourceAllocationsGrid;
import io.imunity.furms.ui.components.resource_allocations.ResourceAllocationsGridItem;
import io.imunity.furms.ui.components.support.models.CheckboxModel;
import io.imunity.furms.ui.user_context.InvocationContext;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaadin.flow.component.ComponentUtil.getData;
import static com.vaadin.flow.component.ComponentUtil.setData;
import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static io.imunity.furms.ui.views.fenix.dashboard.DashboardOptions.INCLUDE_EXPIRED;
import static io.imunity.furms.ui.views.fenix.dashboard.DashboardOptions.INCLUDE_FULLY_DISTRIBUTED;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;
import static java.util.stream.Collectors.toSet;

@Route(value = "fenix/admin/dashboard", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.dashboard.page.title")
public class DashboardView extends FurmsViewComponent {

	private final ResourceCreditService creditService;
	private final ResourceTypeService resourceTypeService;
	private final SiteService siteService;

	private final DashboardViewFilters filters;
	private final ResourceAllocationsGrid grid;
	private final ZoneId browserZoneId;

	DashboardView(ResourceCreditService creditService,
	              ResourceTypeService resourceTypeService,
	              SiteService siteService) {
		this.creditService = creditService;
		this.resourceTypeService = resourceTypeService;
		this.siteService = siteService;
		this.browserZoneId = InvocationContext.getCurrent().getZone();

		this.filters = initializeFilters();
		this.grid = new ResourceAllocationsGrid(
				this::allocateButtonAction,
				this::loadCredits,
				"view.fenix-admin.dashboard.grid.column");

		addTitle();
		addFiltersAndSearch();
		getContent().add(grid);
	}

	private DashboardViewFilters initializeFilters() {
		final DashboardViewFilters filters = new DashboardViewFilters();
		final DashboardViewFilters savedFilters = getData(UI.getCurrent(), DashboardViewFilters.class);
		if (savedFilters != null) {
			filters.setName(savedFilters.getName());
			filters.setIncludeExpired(savedFilters.isIncludeExpired());
			filters.setIncludeFullyDistributed(savedFilters.isIncludeFullyDistributed());
			setData(UI.getCurrent(), DashboardViewFilters.class, null);
		}

		return filters;
	}

	private void addTitle() {
		final ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.fenix-admin.dashboard.title"));
		getContent().add(headerLayout);
	}

	private void addFiltersAndSearch() {
		final CheckboxGroup<CheckboxModel<DashboardOptions>> filters = createFiltersForm();
		final TextField searchForm = createSearchForm();

		final HorizontalLayout layout = new HorizontalLayout(filters, searchForm);
		layout.setWidthFull();
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

		getContent().add(layout);
	}

	private CheckboxGroup<CheckboxModel<DashboardOptions>> createFiltersForm() {
		final CheckboxGroup<CheckboxModel<DashboardOptions>> checkboxGroup = new CheckboxGroup<>();
		checkboxGroup.setItemLabelGenerator(CheckboxModel::getLabel);
		final List<CheckboxModel<DashboardOptions>> values = List.of(
				new CheckboxModel<>(INCLUDE_FULLY_DISTRIBUTED,
						getTranslation("view.fenix-admin.dashboard.filters.fully-distributed")),
				new CheckboxModel<>(INCLUDE_EXPIRED,
						getTranslation("view.fenix-admin.dashboard.filters.expired")));
		checkboxGroup.setItems(values);
		checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
		checkboxGroup.addValueChangeListener(event -> {
			filters.setIncludeFullyDistributed(isSelectedCheckbox(INCLUDE_FULLY_DISTRIBUTED, event.getValue()));
			filters.setIncludeExpired(isSelectedCheckbox(INCLUDE_EXPIRED, event.getValue()));
			UI.getCurrent().accessSynchronously(grid::reloadGrid);
		});
		checkboxGroup.setValue(
			values.stream()
				.filter(value ->
						filters.isIncludeFullyDistributed() && INCLUDE_FULLY_DISTRIBUTED.equals(value.getOption())
						|| filters.isIncludeExpired() && INCLUDE_EXPIRED.equals(value.getOption()))
				.collect(toSet()));

		return checkboxGroup;
	}

	private boolean isSelectedCheckbox(DashboardOptions checkbox, Set<CheckboxModel<DashboardOptions>> value) {
		return value.stream()
				.anyMatch(filter -> checkbox.equals(filter.getOption()));
	}

	private TextField createSearchForm() {
		final TextField textField = new TextField();
		textField.setPlaceholder(getTranslation("view.fenix-admin.dashboard.search.placeholder"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);
		textField.getStyle().set("margin-left", "auto");
		textField.addValueChangeListener(event -> {
			textField.blur();
			filters.setName(event.getValue().toLowerCase());
			UI.getCurrent().accessSynchronously(grid::reloadGrid);
			textField.focus();
		});
		textField.setValue(filters.getName());

		return textField;
	}

	private void allocateButtonAction(ResourceAllocationsGridItem item) {
		ComponentUtil.setData(UI.getCurrent(), ResourceAllocationsGridItem.class, item);
		ComponentUtil.setData(UI.getCurrent(), DashboardViewFilters.class, filters);
		UI.getCurrent().navigate(DashboardResourceAllocateFormView.class);
	}

	private Stream<ResourceAllocationsGridItem> loadCredits() {
		return creditService.findAllWithAllocations(
					filters.getName(),
					filters.isIncludeFullyDistributed(),
					filters.isIncludeExpired())
				.stream()
				.map(this::buildItem);
	}

	private ResourceAllocationsGridItem buildItem(ResourceCreditWithAllocations credit) {
		final ResourceMeasureUnit unit = resourceTypeService.findById(credit.getResourceType().id, credit.getSiteId())
				.map(type -> type.unit)
				.orElse(ResourceMeasureUnit.NONE);

		return ResourceAllocationsGridItem.builder()
				.id(credit.getId())
				.siteId(credit.getSiteId())
				.siteName(findSiteName(credit.getSiteId()))
				.name(credit.getName())
				.split(credit.getSplit())
				.resourceType(credit.getResourceType())
				.credit(createResource(credit.getAmount(), unit))
				.distributed(createResource(calcDistributed(credit), unit))
				.remaining(createResource(credit.getRemaining(), unit))
				.created(convertToZoneTime(credit.getUtcCreateTime(), browserZoneId))
				.validFrom(convertToZoneTime(credit.getUtcStartTime(), browserZoneId))
				.validTo(convertToZoneTime(credit.getUtcEndTime(), browserZoneId))
				.build();
	}

	private BigDecimal calcDistributed(ResourceCreditWithAllocations credit) {
		if (credit == null || credit.getAmount() == null || credit.getRemaining() == null) {
			return BigDecimal.ZERO;
		}
		return credit.getAmount().subtract(credit.getRemaining());
	}

	private String findSiteName(String siteId) {
		return siteService.findById(siteId)
				.map(Site::getName)
				.orElseThrow(() -> new IllegalArgumentException("Incorrect Site ID. Site has not been found."));
	}

	private DashboardGridResource createResource(BigDecimal amount, ResourceMeasureUnit unit) {
		return DashboardGridResource.builder()
				.amount(amount)
				.unit(unit)
				.build();
	}

}
