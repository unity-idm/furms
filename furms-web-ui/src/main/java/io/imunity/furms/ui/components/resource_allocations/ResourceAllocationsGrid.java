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
				.setWidth("10%");
		addColumn(item -> showResource(item.getCredit()))
				.setHeader(getTranslation("component.resource.credit.grid.column.credit"))
				.setWidth("10%");
		addColumn(item -> showResource(item.getConsumed()))
				.setHeader(getTranslation("component.resource.credit.grid.column.consumed"))
				.setWidth("10%");
		addColumn(item -> showResource(item.getRemaining()))
				.setHeader(getTranslation("component.resource.credit.grid.column.remaining"))
				.setWidth("10%");
		addColumn(ResourceAllocationsGridItem::getCreated)
				.setHeader(getTranslation("component.resource.credit.grid.column.created"))
				.setWidth("10%");
		addColumn(ResourceAllocationsGridItem::getValidFrom)
				.setHeader(getTranslation("component.resource.credit.grid.column.valid-from"))
				.setWidth("10%");
		addColumn(ResourceAllocationsGridItem::getValidTo)
				.setHeader(getTranslation("component.resource.credit.grid.column.valid-to"))
				.setWidth("10%");
		addComponentColumn(this::showAvailability)
				.setHeader(getTranslation("component.resource.credit.grid.column.availability"))
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
		final double value = item.getRemaining().getAmount()
				.divide(item.getCredit().getAmount(), 4, HALF_UP)
				.doubleValue();

		return new FurmsProgressBar(value);
	}

	private Component showAllocateButton(ResourceAllocationsGridItem item) {
		if (item.getRemaining() == null || ZERO.compareTo(item.getRemaining().getAmount())!=0) {
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
}
