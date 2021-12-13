/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.ui.views.project.resource_access;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.treegrid.TreeGrid;

@CssImport(value = "./styles/components/dense-grid.css", themeFor = "vaadin-grid")
public class DenseTreeGrid<T> extends TreeGrid<T> {

	public DenseTreeGrid() {
		setDefaults();
	}

	private void setDefaults() {
		setAllRowsVisible(true);
		addThemeVariants(GridVariant.LUMO_NO_BORDER);
		addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
		addClassName("dense-grid");
	}
}
