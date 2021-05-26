/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunk;

import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import static io.imunity.furms.ui.utils.VaadinTranslator.getTranslation;

public class ProjectAllocationDetailsComponentFactory {
	private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static Component create(Set<ProjectAllocationChunk> allocationChunks) {
		Element tableElement = new Element("table");
		tableElement.getStyle().set("width", "90%");
		tableElement.getStyle().set("text-align", "left");
		Element thead = new Element("thead");

		Element theadRow = new Element("tr");
		Element amountHeader = new Element("th");
		amountHeader.setText(getTranslation("view.community-admin.project-allocation.inner.table.1"));
		Element receivedHeader = new Element("th");
		receivedHeader.setText(getTranslation("view.community-admin.project-allocation.inner.table.2"));
		Element validFromHeader = new Element("th");
		validFromHeader.setText(getTranslation("view.community-admin.project-allocation.inner.table.3"));
		Element validToHeader = new Element("th");
		validToHeader.setText(getTranslation("view.community-admin.project-allocation.inner.table.4"));
		theadRow.appendChild(amountHeader, receivedHeader, validFromHeader, validToHeader);
		thead.appendChild(theadRow);

		Element tbody = new Element("tbody");
		for(ProjectAllocationChunk chunk: allocationChunks){
			Element row = new Element("tr");
			Element amountField = new Element("td");
			amountField.setText(Optional.ofNullable(chunk.amount).map(Object::toString).orElse(""));
			Element receivedField = new Element("td");
			receivedField.setText(Optional.ofNullable(chunk.receivedTime).map(t -> t.format(dateTimeFormatter)).orElse(""));
			Element validFrom = new Element("td");
			validFrom.setText(Optional.ofNullable(chunk.validFrom).map(t -> t.format(dateTimeFormatter)).orElse(""));
			Element validTo = new Element("td");
			validTo.setText(Optional.ofNullable(chunk.validTo).map(t -> t.format(dateTimeFormatter)).orElse(""));
			row.appendChild(amountField, receivedField, validFrom, validTo);
			tbody.appendChild(row);
		}
		tableElement.appendChild(thead, tbody);
		Div div = new Div();
		div.getElement().appendChild(tableElement);
		return div;
	}
}
