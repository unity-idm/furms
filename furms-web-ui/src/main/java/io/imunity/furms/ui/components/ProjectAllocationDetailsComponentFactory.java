/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;
import io.imunity.furms.ui.user_context.InvocationContext;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;
import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

public class ProjectAllocationDetailsComponentFactory {
	private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static Component create(Set<ProjectAllocationChunk> allocationChunks) {
		ZoneId browserZoneId = InvocationContext.getCurrent().getZone();
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

		Tbody tbody = new Tbody();
		for(ProjectAllocationChunk chunk: allocationChunks){
			Tr row = new Tr();
			Td amountField = new Td();
			amountField.setText(Optional.ofNullable(chunk.amount).map(Object::toString).orElse(""));
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
		Div div = new Div();
		div.getElement().appendChild(tableElement);
		return div;
	}

	private static class Td extends Element {
		public Td() {
			super("td");
		}
	}

	private static class Th extends Element {
		public Th() {
			super("th");
		}
	}

	private static class Tr extends Element {
		public Tr() {
			super("tr");
		}
	}

	private static class Tbody extends Element {
		public Tbody() {
			super("tbody");
		}
	}
}
