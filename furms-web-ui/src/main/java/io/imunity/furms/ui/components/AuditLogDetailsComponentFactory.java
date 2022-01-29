/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import static io.imunity.furms.utils.UTCTimeUtils.convertToZoneTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.dom.Element;

import io.imunity.furms.ui.user_context.UIContext;

public class AuditLogDetailsComponentFactory {
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static Component create(Map<String, Object> data) {
		if(data == null)
			return new Div();
		ZoneId browserZoneId = UIContext.getCurrent().getZone();
		Element tableElement = new Element("table");
		tableElement.getStyle().set("text-align", "left");
		Element thead = new Element("thead");

		TBody tbody = new TBody();

		data.forEach((key, value) -> {
			Tr row = new Tr();
			Td createdTd = new Td();
			createdTd.setText(toHumanReadableString(key));
			createdTd.getStyle().set("font-weight", "bold");
			createdTd.getStyle().set("width", "11em");
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
		Div div = new AuditLogDetails();
		div.getElement().appendChild(tableElement);
		return div;
	}
	
    private static String toHumanReadableString(String str)
    {
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toUpperCase(str.charAt(0)));
		for (int i = 1; i < str.length(); i++)
		{
			char charAt = str.charAt(i);
			if (Character.isUpperCase(charAt))
			{
				sb.append(" " + Character.toLowerCase(charAt));
			} else
			{
				sb.append(charAt);
			}
		}
		return sb.toString();
    }
	
	@CssImport(value = "./styles/components/allocation-details.css")
	private static class AuditLogDetails extends Div {
		AuditLogDetails() {
			super();
			this.addClassName("allocation-details");
		}
	}
}
