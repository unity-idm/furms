/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

@CssImport(value = "./styles/components/dense-grid.css", themeFor = "vaadin-grid")
public class DenseGrid<T> extends Grid<T> {

	public DenseGrid(Class<T> beanType) {
		super(beanType, false);
		setDefaults();
	}

	private void setDefaults() {
		setAllRowsVisible(true);
		addThemeVariants(GridVariant.LUMO_NO_BORDER);
		addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		addClassName("dense-grid");
	}
}
