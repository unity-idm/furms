/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;

import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.ViewMode;

public class FurmsSelect extends Select<FurmsSelectText> {
	public FurmsSelect(Map<ViewMode, List<FurmsViewUserContext>> data) {
		List<FurmsSelectText> items = data.values().stream()
			.flatMap(Collection::stream)
			.map(FurmsSelectText::new)
			.collect(toList());
		setItems(items);
		//addSeparators(data); TODO FIX separators are disabled now
		setTextRenderer(Text::getText);
		addValueChangeListener(event -> {
			UI.getCurrent().getSession().setAttribute(FurmsViewUserContext.class, event.getValue().furmsViewUserContext);
			UI.getCurrent().navigate(event.getValue().furmsViewUserContext.route);
		});
		ofNullable(UI.getCurrent().getSession().getAttribute(FurmsViewUserContext.class))
			.ifPresent(userContext -> setValue(new FurmsSelectText(userContext)));
	}

	@SuppressWarnings("unused")
	private void addSeparators(Map<ViewMode, List<FurmsViewUserContext>> data) {
		FurmsSelectText component = null;
		for (Map.Entry<ViewMode, List<FurmsViewUserContext>> entry : data.entrySet()) {
			if(entry.getKey() == ViewMode.USER){
				continue;
			}
			if(component != null){
				Span text = new Span(entry.getKey().name());
				text.addClassName("select-span-separator");
				addComponents(component, new Hr());
				addComponents(component, text);
				addComponents(component, new Hr());
			}
			if(entry.getValue().size() > 0){
				component = new FurmsSelectText(entry.getValue().get(entry.getValue().size() - 1));
			}
		}
	}
}
