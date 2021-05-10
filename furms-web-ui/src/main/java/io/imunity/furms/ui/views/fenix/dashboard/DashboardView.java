/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.dashboard;

import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static io.imunity.furms.ui.views.fenix.dashboard.DashboardOptions.INCLUDE_EXPIRED;
import static io.imunity.furms.ui.views.fenix.dashboard.DashboardOptions.INCLUDE_FULLY_DISTRIBUTED;
import static java.util.Optional.ofNullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Stream;

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
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

@Route(value = "fenix/admin/dashboard", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.dashboard.page.title")
public class DashboardView extends FurmsViewComponent {

	private final ResourceCreditService creditService;
	private final ResourceTypeService resourceTypeService;
	private final SiteService siteService;

	private final DashboardViewFilters filters;
	private final ResourceAllocationsGrid grid;

	DashboardView(ResourceCreditService creditService,
	              ResourceTypeService resourceTypeService,
	              SiteService siteService) {
		this.creditService = creditService;
		this.resourceTypeService = resourceTypeService;
		this.siteService = siteService;

		this.filters = new DashboardViewFilters();
		this.grid = new ResourceAllocationsGrid(this::allocateButtonAction, this::loadCredits);

		addTitle();
		addFiltersAndSearch();
		getContent().add(grid);
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
		checkboxGroup.setItems(
				new CheckboxModel<>(INCLUDE_FULLY_DISTRIBUTED,
						getTranslation("view.fenix-admin.dashboard.filters.fully-distributed")),
				new CheckboxModel<>(INCLUDE_EXPIRED,
						getTranslation("view.fenix-admin.dashboard.filters.expired")));
		checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
		checkboxGroup.addValueChangeListener(event -> {
			filters.setIncludeFullyDistributed(isSelectedCheckbox(INCLUDE_FULLY_DISTRIBUTED, event.getValue()));
			filters.setIncludeExpired(isSelectedCheckbox(INCLUDE_EXPIRED, event.getValue()));
			UI.getCurrent().accessSynchronously(grid::reloadGrid);
		});
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

		return textField;
	}

	private void allocateButtonAction(ResourceAllocationsGridItem item) {
		ComponentUtil.setData(UI.getCurrent(), ResourceAllocationsGridItem.class, item);
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
		final ResourceMeasureUnit unit = resourceTypeService.findById(credit.getResourceTypeId(), credit.getSiteId())
				.map(type -> type.unit)
				.orElse(ResourceMeasureUnit.SiUnit.none);

		return ResourceAllocationsGridItem.builder()
				.id(credit.getId())
				.siteId(credit.getSiteId())
				.siteName(findSiteName(credit.getSiteId()))
				.name(credit.getName())
				.split(credit.getSplit())
				.resourceTypeId(credit.getResourceTypeId())
				.credit(createResource(credit.getAmount(), unit))
				.consumed(createResource(calcConsumed(credit), unit))
				.remaining(createResource(credit.getRemaining(), unit))
				.created(extractLocalDate(credit.getUtcCreateTime()))
				.validFrom(extractLocalDate(credit.getUtcStartTime()))
				.validTo(extractLocalDate(credit.getUtcEndTime()))
				.build();
	}

	private BigDecimal calcConsumed(ResourceCreditWithAllocations credit) {
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

	private LocalDate extractLocalDate(LocalDateTime dateTime) {
		return ofNullable(dateTime)
				.map(LocalDateTime::toLocalDate)
				.orElse(null);
	}
}
