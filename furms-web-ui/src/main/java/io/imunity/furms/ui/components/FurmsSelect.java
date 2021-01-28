/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.select.Select;
import io.imunity.furms.ui.user_context.FurmsViewUserContext;
import io.imunity.furms.ui.user_context.RoleTranslator;
import io.imunity.furms.ui.user_context.ViewMode;

import java.util.List;
import java.util.Map;

public class FurmsSelect extends Select<FurmsSelectText> {
	private final FurmsSelectService furmsSelectService;
	public FurmsSelect(RoleTranslator roleTranslator) {
		furmsSelectService = new FurmsSelectService(roleTranslator);
		List<FurmsSelectText> items = furmsSelectService.loadItems();

		setItems(items);
		setTextRenderer(Text::getText);
		//addSeparators(data); TODO FIX separators are disabled now

		furmsSelectService.loadSelectedItem()
			.ifPresent(userContext -> setValue(new FurmsSelectText(userContext)));

		addValueChangeListener(event -> furmsSelectService.manageSelectedItemRedirects(event.getValue()));
	}

	void reloadComponent(){
		setItems(furmsSelectService.reloadItems());
		furmsSelectService.loadSelectedItem()
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
