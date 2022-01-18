/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.domain.resource_types.AmountWithUnit;
import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.ui.user_context.UIContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

public class AllocationDetailsComponentFactory {
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static Component create(Set<ProjectAllocationChunk> allocationChunks, ResourceMeasureUnit unit) {
		ZoneId browserZoneId = UIContext.getCurrent().getZone();
		Element tableElement = new Element("table");
		tableElement.getStyle().set("width", "90%");
		tableElement.getStyle().set("text-align", "left");
		Element thead = new Element("thead");

		Tr theadRow = new Tr();
		Th amountHeader = new Th();
		amountHeader.setText(getTranslation("view.community-admin.project-allocation.inner.table.1"));
		Th receivedHeader = new Th();
		receivedHeader.setText(getTranslation("view.community-admin.project-allocation.inner.table.2"));
		Th validFromHeader = new Th();
		validFromHeader.setText(getTranslation("view.community-admin.project-allocation.inner.table.3"));
		Th validToHeader = new Th();
		validToHeader.setText(getTranslation("view.community-admin.project-allocation.inner.table.4"));
		theadRow.appendChild(amountHeader, receivedHeader, validFromHeader, validToHeader);
		thead.appendChild(theadRow);

		TBody tbody = new TBody();
		List<ProjectAllocationChunk> orderedChunks = allocationChunks.stream()
			.sorted(Comparator.comparing((ProjectAllocationChunk chunk) -> chunk.validFrom)
				.thenComparing(chunk -> chunk.validTo)
			).collect(Collectors.toList());
		for(ProjectAllocationChunk chunk: orderedChunks){
			Tr row = new Tr();
			Td amountField = new Td();
			amountField.setText(Optional.ofNullable(chunk.amount)
					.map(a -> new AmountWithUnit(a, unit).toString()).orElse(""));
			Td receivedField = new Td();
			receivedField.setText(Optional.ofNullable(chunk.receivedTime)
				.map(t -> convertToZoneTime(t, browserZoneId))
				.map(t -> t.format(dateTimeFormatter))
				.orElse(""));
			Td validFrom = new Td();
			validFrom.setText(Optional.ofNullable(chunk.validFrom)
				.map(t -> convertToZoneTime(t, browserZoneId))
				.map(t -> t.format(dateTimeFormatter))
				.orElse(""));
			Td validTo = new Td();
			validTo.setText(Optional.ofNullable(chunk.validTo)
				.map(t -> convertToZoneTime(t, browserZoneId))
				.map(t -> t.format(dateTimeFormatter))
				.orElse(""));
			row.appendChild(amountField, receivedField, validFrom, validTo);
			tbody.appendChild(row);
		}
		tableElement.appendChild(thead, tbody);
		Div div = new AllocationDetails();
		div.getElement().appendChild(tableElement);
		return div;
	}

	public static Component create(LocalDateTime created, LocalDateTime validFrom, LocalDateTime validTo) {
		ZoneId browserZoneId = UIContext.getCurrent().getZone();
		Element tableElement = new Element("table");
		tableElement.getStyle().set("width", "50%");
		tableElement.getStyle().set("text-align", "left");
		Element thead = new Element("thead");

		TBody tbody = new TBody();

		Tr row = new Tr();
		Td createdTd = new Td();
		createdTd.setText(getTranslation("table.created"));
		createdTd.getStyle().set("font-weight", "bold");
		Td createdTimeTd = new Td();
		createdTimeTd.setText(Optional.ofNullable(created)
			.map(t -> convertToZoneTime(t, browserZoneId))
			.map(t -> t.format(dateTimeFormatter))
			.orElse(""));
		row.appendChild(createdTd, createdTimeTd);
		tbody.appendChild(row);

		Tr row1 = new Tr();
		Td validFromTd = new Td();
		validFromTd.setText(getTranslation("table.valid.from"));
		validFromTd.getStyle().set("font-weight", "bold");
		Td validFromTimeTd = new Td();
		validFromTimeTd.setText(Optional.ofNullable(validFrom)
			.map(t -> convertToZoneTime(t, browserZoneId))
			.map(t -> t.format(dateTimeFormatter))
			.orElse(""));
		row1.appendChild(validFromTd, validFromTimeTd);
		tbody.appendChild(row1);


		Tr row2 = new Tr();
		Td validToTd = new Td();
		validToTd.setText(getTranslation("table.valid.to"));
		validToTd.getStyle().set("font-weight", "bold");
		Td validToTimeTd = new Td();
		validToTimeTd.setText(Optional.ofNullable(validTo)
			.map(t -> convertToZoneTime(t, browserZoneId))
			.map(t -> t.format(dateTimeFormatter))
			.orElse(""));

		row2.appendChild(validToTd, validToTimeTd);
		tbody.appendChild(row2);


		tableElement.appendChild(thead, tbody);
		Div div = new AllocationDetails();
		div.getElement().appendChild(tableElement);
		return div;
	}

	public static Component create(Map<String, Object> data) {
		ZoneId browserZoneId = UIContext.getCurrent().getZone();
		Element tableElement = new Element("table");
		tableElement.getStyle().set("width", "50%");
		tableElement.getStyle().set("text-align", "left");
		Element thead = new Element("thead");

		TBody tbody = new TBody();

		data.forEach((key, value) -> {
			Tr row = new Tr();
			Td createdTd = new Td();
			createdTd.setText(key);
			createdTd.getStyle().set("font-weight", "bold");
			Td createdTimeTd = new Td();
			createdTimeTd.setText(Optional.ofNullable(value)
				.map(t -> {
					if(value instanceof LocalDateTime)
						return convertToZoneTime((LocalDateTime)value, browserZoneId).format(dateTimeFormatter);
					else
						return value.toString();
				})
				.orElse(""));
			row.appendChild(createdTd, createdTimeTd);
			tbody.appendChild(row);
		});

		tableElement.appendChild(thead, tbody);
		Div div = new AllocationDetails();
		div.getElement().appendChild(tableElement);
		return div;
	}
	
	@CssImport(value = "./styles/components/allocation-details.css")
	private static class AllocationDetails extends Div
	{
		AllocationDetails() {
			super();
			this.addClassName("allocation-details");
		}
	}
}
