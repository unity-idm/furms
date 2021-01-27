/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class FurmsSelect extends Select<FurmsSelectText> {
	
	public FurmsSelect(Map<ViewMode, List<FurmsViewUserContext>> data) {
		List<FurmsSelectText> items = loadItems(data);
		setItems(items);
		ofNullable(UI.getCurrent().getSession().getAttribute(FurmsViewUserContext.class))
			.ifPresent(userContext -> setValue(new FurmsSelectText(userContext)));
		//addSeparators(data); TODO FIX separators are disabled now
		setTextRenderer(Text::getText);

		addValueChangeListener(event -> {
			UI.getCurrent().getSession().setAttribute(
				FurmsViewUserContext.class,
				ofNullable(event.getValue())
					.orElse(event.getOldValue())
					.furmsViewUserContext
			);
			if(event.getValue() != null && event.getValue().furmsViewUserContext.redirectable){
				UI.getCurrent().navigate(event.getValue().furmsViewUserContext.route);
			}
		});
	}

	void reloadComponent(Map<ViewMode, List<FurmsViewUserContext>> data){
		List<FurmsSelectText> items = loadItems(data);
		String id = ofNullable(UI.getCurrent().getSession().getAttribute(FurmsViewUserContext.class))
			.map(x -> x.id)
			.orElse(null);
		items.stream()
			.filter(selectText -> selectText.furmsViewUserContext.id.equals(id))
			.findAny()
			.ifPresent(furmsSelectText -> {
				items.remove(furmsSelectText);
				FurmsSelectText selectText = new FurmsSelectText(
					new FurmsViewUserContext(furmsSelectText.furmsViewUserContext, false)
				);
				items.add(selectText);
				setItems(items);
				setValue(selectText);
			});
	}

	private List<FurmsSelectText> loadItems(Map<ViewMode, List<FurmsViewUserContext>> data) {
		return data.values().stream()
			.flatMap(Collection::stream)
			.map(FurmsSelectText::new)
			.collect(toList());
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
