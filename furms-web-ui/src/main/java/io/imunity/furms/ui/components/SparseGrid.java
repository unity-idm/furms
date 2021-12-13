/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.components;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

@CssImport(value = "./styles/components/sparse-grid.css", themeFor = "vaadin-grid")
public class SparseGrid<T> extends Grid<T> {

	public SparseGrid(Class<T> beanType) {
		super(beanType, false);
		setDefaults();
	}

	private void setDefaults() {
		setAllRowsVisible(true);
		addThemeVariants(GridVariant.LUMO_NO_BORDER);
		addClassName("sparse-grid");
	}
}
