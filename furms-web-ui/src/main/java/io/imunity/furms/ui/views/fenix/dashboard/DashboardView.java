/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.fenix.dashboard;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import io.imunity.furms.api.resource_credits.ResourceCreditService;
import io.imunity.furms.api.resource_types.ResourceTypeService;
import io.imunity.furms.api.sites.SiteService;
import io.imunity.furms.domain.resource_credits.ResourceCreditFenixDashboard;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.sites.Site;
import io.imunity.furms.ui.components.FurmsProgressBar;
import io.imunity.furms.ui.components.FurmsViewComponent;
import io.imunity.furms.ui.components.PageTitle;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.components.ViewHeaderLayout;
import io.imunity.furms.ui.views.fenix.dashboard.allocate.DashboardResourceAllocateFormView;
import io.imunity.furms.ui.views.fenix.menu.FenixAdminMenu;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;
import static io.imunity.furms.ui.views.fenix.dashboard.DashboardViewFilters.Checkboxes.Options.INCLUDED_EXPIRED;
import static io.imunity.furms.ui.views.fenix.dashboard.DashboardViewFilters.Checkboxes.Options.INCLUDED_FULLY_DISTRIBUTED;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;

@Route(value = "fenix/admin/dashboard", layout = FenixAdminMenu.class)
@PageTitle(key = "view.fenix-admin.dashboard.page.title")
public class DashboardView extends FurmsViewComponent {

	private final ResourceCreditService creditService;
	private final ResourceTypeService resourceTypeService;
	private final SiteService siteService;

	private final SparseGrid<DashboardGridItem> grid;
	private final Comparator<DashboardGridItem> defaultGridSort;
	private final DashboardViewFilters filters;

	DashboardView(ResourceCreditService creditService,
	              ResourceTypeService resourceTypeService,
	              SiteService siteService) {
		this.creditService = creditService;
		this.resourceTypeService = resourceTypeService;
		this.siteService = siteService;

		this.grid = new SparseGrid<>(DashboardGridItem.class);
		this.defaultGridSort = comparing(DashboardGridItem::getSiteName);
		this.filters = new DashboardViewFilters();

		addTitle();
		addFiltersAndSearch();
		addGrid();
	}

	private void addTitle() {
		final ViewHeaderLayout headerLayout = new ViewHeaderLayout(getTranslation("view.fenix-admin.dashboard.title"));
		getContent().add(headerLayout);
	}

	private void addFiltersAndSearch() {
		final CheckboxGroup<DashboardViewFilters.Checkboxes> filters = createFiltersForm();
		final TextField searchForm = createSearchForm();

		final HorizontalLayout layout = new HorizontalLayout(filters, searchForm);
		layout.setWidthFull();
		layout.setAlignItems(FlexComponent.Alignment.CENTER);
		layout.setJustifyContentMode(FlexComponent.JustifyContentMode.START);

		getContent().add(layout);
	}

	private CheckboxGroup<DashboardViewFilters.Checkboxes> createFiltersForm() {
		final CheckboxGroup<DashboardViewFilters.Checkboxes> checkboxGroup = new CheckboxGroup<>();
		checkboxGroup.setLabel(getTranslation("view.fenix-admin.dashboard.filters.title"));
		checkboxGroup.setItemLabelGenerator(DashboardViewFilters.Checkboxes::getLabel);
		checkboxGroup.setItems(
				new DashboardViewFilters.Checkboxes(INCLUDED_FULLY_DISTRIBUTED,
						getTranslation("view.fenix-admin.dashboard.filters.fully-distributed")),
				new DashboardViewFilters.Checkboxes(INCLUDED_EXPIRED,
						getTranslation("view.fenix-admin.dashboard.filters.expired")));
		checkboxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
		checkboxGroup.addValueChangeListener(event -> {
			filters.setIncludedFullyDistributed(isSelectedCheckbox(INCLUDED_FULLY_DISTRIBUTED, event.getValue()));
			filters.setIncludedExpired(isSelectedCheckbox(INCLUDED_EXPIRED, event.getValue()));
			UI.getCurrent().accessSynchronously(this::reloadGrid);
		});
		return checkboxGroup;
	}

	private boolean isSelectedCheckbox(DashboardViewFilters.Checkboxes.Options checkbox,
	                                   Set<DashboardViewFilters.Checkboxes> value) {
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
			UI.getCurrent().accessSynchronously(this::reloadGrid);
			textField.focus();
		});

		return textField;
	}

	private void addGrid() {
		grid.getStyle().set("word-wrap", "break-word");
		grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

		grid.addColumn(DashboardGridItem::getSiteName)
				.setHeader(getTranslation("view.fenix-admin.dashboard.grid.column.site-name"))
				.setSortable(true)
				.setComparator(defaultGridSort)
				.setFlexGrow(18);
		grid.addColumn(DashboardGridItem::getName)
				.setHeader(getTranslation("view.fenix-admin.dashboard.grid.column.name"))
				.setFlexGrow(18);
		grid.addColumn(item -> showResource(item.getCredit()))
				.setHeader(getTranslation("view.fenix-admin.dashboard.grid.column.credit"))
				.setFlexGrow(10);
		grid.addColumn(item -> showResource(item.getRemaining()))
				.setHeader(getTranslation("view.fenix-admin.dashboard.grid.column.remaining"))
				.setFlexGrow(10);
		grid.addColumn(DashboardGridItem::getCreated)
				.setHeader(getTranslation("view.fenix-admin.dashboard.grid.column.created"))
				.setFlexGrow(7);
		grid.addColumn(DashboardGridItem::getValidFrom)
				.setHeader(getTranslation("view.fenix-admin.dashboard.grid.column.valid-from"))
				.setFlexGrow(7);
		grid.addColumn(DashboardGridItem::getValidTo)
				.setHeader(getTranslation("view.fenix-admin.dashboard.grid.column.valid-to"))
				.setFlexGrow(7);
		grid.addComponentColumn(this::showAvailability)
				.setHeader(getTranslation("view.fenix-admin.dashboard.grid.column.availability"))
				.setFlexGrow(22);
		grid.addComponentColumn(this::showAllocateButton)
				.setWidth("3em")
				.setFlexGrow(1);

		reloadGrid();
		getContent().add(grid);
	}

	private FurmsProgressBar showAvailability(DashboardGridItem item) {
		final Double value = item.getRemaining().getAmount()
				.divide(item.getCredit().getAmount(), 4, HALF_UP)
				.doubleValue();

		return new FurmsProgressBar(value);
	}

	private Component showAllocateButton(DashboardGridItem item) {
		if (item.getRemaining() == null || ZERO.compareTo(item.getRemaining().getAmount())!=0) {
			final Button plus = new Button(new Icon(PLUS_CIRCLE));
			plus.addClickListener(event -> {
				ComponentUtil.setData(UI.getCurrent(), DashboardGridItem.class, item);
				UI.getCurrent().navigate(DashboardResourceAllocateFormView.class);
			});
			plus.addThemeVariants(LUMO_TERTIARY);
			return plus;
		}
		return new Div();
	}

	private Object showResource(DashboardGridResource item) {
		return format("%s %s", item.getAmount(), item.getUnit().name());
	}

	private void reloadGrid() {
		grid.setItems(loadCredits());
	}

	private List<DashboardGridItem> loadCredits() {
		return creditService.findAllForFenixAdminDashboard(
					filters.getName(),
					filters.isIncludedFullyDistributed(),
					filters.isIncludedExpired())
				.stream()
				.map(this::buildItem)
				.sorted(defaultGridSort)
				.collect(Collectors.toList());
	}

	private DashboardGridItem buildItem(ResourceCreditFenixDashboard credit) {
		final ResourceMeasureUnit unit = resourceTypeService.findById(credit.resourceTypeId)
				.map(type -> type.unit)
				.orElse(ResourceMeasureUnit.SiUnit.none);

		return DashboardGridItem.builder()
				.id(credit.id)
				.siteId(credit.siteId)
				.siteName(findSiteName(credit.siteId))
				.name(credit.name)
				.split(credit.split)
				.resourceTypeId(credit.resourceTypeId)
				.credit(createResource(credit.amount, unit))
				.remaining(createResource(credit.remaining, unit))
				.created(extractLocalDate(credit.utcCreateTime))
				.validFrom(extractLocalDate(credit.utcStartTime))
				.validTo(extractLocalDate(credit.utcEndTime))
				.build();
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
