/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.resource_allocations;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import io.imunity.furms.ui.components.FurmsProgressBar;
import io.imunity.furms.ui.components.SparseGrid;
import io.imunity.furms.ui.views.fenix.dashboard.DashboardGridResource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@CssImport("./styles/components/resource-allocation-grid.css")
public class ResourceAllocationsGrid extends SparseGrid<ResourceAllocationsGridItem>{

	private final Consumer<ResourceAllocationsGridItem> allocateButtonAction;
	private final Supplier<Stream<ResourceAllocationsGridItem>> fetchItems;

	private final Comparator<ResourceAllocationsGridItem> defaultGridSort;

	public ResourceAllocationsGrid(Consumer<ResourceAllocationsGridItem> allocateButtonAction,
	                               Supplier<Stream<ResourceAllocationsGridItem>> fetchItems) {
		super(ResourceAllocationsGridItem.class);

		this.defaultGridSort = comparing(ResourceAllocationsGridItem::getSiteName);

		this.allocateButtonAction = allocateButtonAction;
		this.fetchItems = fetchItems;

		addClassName("resource-allocation-grid");

		addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

		addColumn(ResourceAllocationsGridItem::getSiteName)
				.setHeader(getTranslation("component.resource.credit.grid.column.site-name"))
				.setSortable(true)
				.setComparator(defaultGridSort)
				.setWidth("10%");
		addColumn(ResourceAllocationsGridItem::getName)
				.setHeader(getTranslation("component.resource.credit.grid.column.name"))
				.setComparator(comparing(ResourceAllocationsGridItem::getName))
				.setWidth("10%");
		addColumn(item -> showResource(item.getCredit()))
				.setHeader(getTranslation("component.resource.credit.grid.column.credit"))
				.setComparator(comparing(item -> item.getCredit().getAmount()))
				.setWidth("10%");
		addColumn(item -> showResource(item.getDistributed()))
				.setHeader(getTranslation("component.resource.credit.grid.column.distributed"))
				.setComparator(comparing(item -> item.getDistributed().getAmount()))
				.setWidth("10%");
		addColumn(item -> showResource(item.getRemaining()))
				.setHeader(getTranslation("component.resource.credit.grid.column.remaining"))
				.setComparator(comparing(item -> item.getRemaining().getAmount()))
				.setWidth("10%");
		addColumn(item -> extractLocalDate(item.getCreated()))
				.setHeader(getTranslation("component.resource.credit.grid.column.created"))
				.setComparator(comparing(ResourceAllocationsGridItem::getCreated))
				.setWidth("10%");
		addColumn(item -> extractLocalDate(item.getValidFrom()))
				.setHeader(getTranslation("component.resource.credit.grid.column.valid-from"))
				.setComparator(comparing(ResourceAllocationsGridItem::getValidFrom))
				.setWidth("10%");
		addColumn(item -> extractLocalDate(item.getValidTo()))
				.setHeader(getTranslation("component.resource.credit.grid.column.valid-to"))
				.setComparator(comparing(ResourceAllocationsGridItem::getValidTo))
				.setWidth("10%");
		addComponentColumn(this::showAvailability)
				.setHeader(getTranslation("component.resource.credit.grid.column.availability"))
				.setComparator(comparing(this::calcAvailability))
				.setWidth("15%");
		addComponentColumn(this::showAllocateButton)
				.setWidth("3%");

		reloadGrid();
	}

	public void reloadGrid() {
		setItems(fetchItems.get()
					.sorted(defaultGridSort)
					.collect(toList()));
	}

	private FurmsProgressBar showAvailability(ResourceAllocationsGridItem item) {
		final double value = calcAvailability(item);
		return new FurmsProgressBar(value);
	}

	private double calcAvailability(ResourceAllocationsGridItem item) {
		return item.getRemaining().getAmount()
				.divide(item.getCredit().getAmount(), 4, HALF_UP)
				.doubleValue();
	}

	private Component showAllocateButton(ResourceAllocationsGridItem item) {
		if (item.getRemaining() == null
				|| (ZERO.compareTo(item.getRemaining().getAmount())!=0)
					&& !Instant.now().isAfter(item.getValidTo().toInstant(ZoneOffset.UTC))) {
			final Button plus = new Button(new Icon(PLUS_CIRCLE));
			plus.addClickListener(event -> allocateButtonAction.accept(item));
			plus.addThemeVariants(LUMO_TERTIARY);
			return plus;
		}
		return new Div();
	}

	private Object showResource(DashboardGridResource item) {
		return format("%s %s", item.getAmount(), item.getUnit().name());
	}

	private LocalDate extractLocalDate(LocalDateTime dateTime) {
		return ofNullable(dateTime)
				.map(LocalDateTime::toLocalDate)
				.orElse(null);
	}
}
