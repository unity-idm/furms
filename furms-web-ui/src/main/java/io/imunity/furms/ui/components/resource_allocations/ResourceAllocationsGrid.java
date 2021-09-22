/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.resource_allocations;

import static com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY;
import static com.vaadin.flow.component.icon.VaadinIcon.PLUS_CIRCLE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;

import io.imunity.furms.ui.components.DenseGrid;
import io.imunity.furms.ui.components.FurmsProgressBar;
import io.imunity.furms.ui.user_context.UIContext;
import io.imunity.furms.ui.views.fenix.dashboard.DashboardGridResource;

public class ResourceAllocationsGrid extends DenseGrid<ResourceAllocationsGridItem>{

	private final Consumer<ResourceAllocationsGridItem> allocateButtonAction;
	private final Supplier<Stream<ResourceAllocationsGridItem>> fetchItems;

	private final Comparator<ResourceAllocationsGridItem> defaultGridSort;

	private final ZoneId zoneId;

	public ResourceAllocationsGrid(Consumer<ResourceAllocationsGridItem> allocateButtonAction,
	                               Supplier<Stream<ResourceAllocationsGridItem>> fetchItems,
	                               String columnPrefixes) {
		super(ResourceAllocationsGridItem.class);

		this.defaultGridSort = comparing(ResourceAllocationsGridItem::getSiteName);

		this.allocateButtonAction = allocateButtonAction;
		this.fetchItems = fetchItems;

		this.zoneId = UIContext.getCurrent().getZone();

		addColumn(ResourceAllocationsGridItem::getSiteName)
				.setHeader(columnName(columnPrefixes, "site-name"))
				.setSortable(true)
				.setComparator(defaultGridSort)
				.setFlexGrow(1)
				.setWidth("6%");
		addColumn(ResourceAllocationsGridItem::getName)
				.setHeader(columnName(columnPrefixes, "name"))
				.setComparator(comparing(ResourceAllocationsGridItem::getName))
				.setFlexGrow(1)
				.setWidth("11%");
		addColumn(item -> showResource(item.getCredit()))
				.setHeader(columnName(columnPrefixes, "credit"))
				.setComparator(comparing(item -> item.getCredit().getAmount()))
				.setFlexGrow(0)
				.setAutoWidth(true);
		addColumn(item -> showResource(item.getDistributed()))
				.setHeader(columnName(columnPrefixes, "distributed"))
				.setComparator(comparing(item -> item.getDistributed().getAmount()))
				.setFlexGrow(0)
				.setAutoWidth(true);
		addColumn(item -> showResource(item.getRemaining()))
				.setHeader(columnName(columnPrefixes, "remaining"))
				.setComparator(comparing(item -> item.getRemaining().getAmount()))
				.setFlexGrow(0)
				.setAutoWidth(true);
		addColumn(item -> extractLocalDate(item.getCreated()))
				.setHeader(columnName(columnPrefixes, "created"))
				.setComparator(comparing(ResourceAllocationsGridItem::getCreated))
				.setFlexGrow(0)
				.setWidth("10%");
		addColumn(item -> extractLocalDate(item.getValidFrom()))
				.setHeader(columnName(columnPrefixes, "valid-from"))
				.setComparator(comparing(ResourceAllocationsGridItem::getValidFrom))
				.setFlexGrow(0)
				.setWidth("10%");
		addColumn(item -> extractLocalDate(item.getValidTo()))
				.setHeader(columnName(columnPrefixes, "valid-to"))
				.setComparator(comparing(ResourceAllocationsGridItem::getValidTo))
				.setFlexGrow(0)
				.setWidth("10%");
		addComponentColumn(this::showAvailability)
				.setHeader(columnName(columnPrefixes, "availability"))
				.setComparator(comparing(this::calcAvailability))
				.setFlexGrow(4)
				.setAutoWidth(true);
		addComponentColumn(this::showAllocateButton)
				.setFlexGrow(0)
				.setWidth("4%");

		reloadGrid();
	}

	private String columnName(String columnPrefix, String name) {
		return getTranslation(columnPrefix+"."+name);
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
					&& !ZonedDateTime.now(zoneId).isAfter(item.getValidTo())) {
			final Button plus = new Button(new Icon(PLUS_CIRCLE));
			plus.addClickListener(event -> allocateButtonAction.accept(item));
			plus.addThemeVariants(LUMO_TERTIARY);
			return plus;
		}
		return new Div();
	}

	private Object showResource(DashboardGridResource item) {
		return format("%s %s", item.getAmount(), item.getUnit().getSuffix());
	}

	private LocalDate extractLocalDate(ZonedDateTime dateTime) {
		return ofNullable(dateTime)
				.map(ZonedDateTime::toLocalDate)
				.orElse(null);
	}
}
