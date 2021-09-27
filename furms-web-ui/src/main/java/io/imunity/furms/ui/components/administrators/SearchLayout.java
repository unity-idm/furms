/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.administrators;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

import static com.vaadin.flow.component.icon.VaadinIcon.SEARCH;

public class SearchLayout extends HorizontalLayout {
	private TextField textField;
	private String searchText = "";

	public SearchLayout() {
		textField = new TextField();
		textField.setPlaceholder(getTranslation("component.administrators.field.search"));
		textField.setPrefixComponent(SEARCH.create());
		textField.setValueChangeMode(ValueChangeMode.EAGER);

		HorizontalLayout search = new HorizontalLayout(textField);
		search.setWidthFull();
		search.setAlignItems(Alignment.END);
		search.setJustifyContentMode(JustifyContentMode.END);

		add(search);
	}

	public String getSearchText() {
		return searchText;
	}

	public void addValueChangeGridReloader(Runnable gridReloader){
		textField.addValueChangeListener(event -> {
				textField.blur();
				searchText = event.getValue().toLowerCase();
				UI.getCurrent().accessSynchronously(gridReloader::run);
				textField.focus();
		});
	}
}
