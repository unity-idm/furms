/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components;

public class FilterCheckboxData<T> {

	private final T option;
	private final String label;

	public FilterCheckboxData(T option, String label) {
		this.option = option;
		this.label = label;
	}

	public T getOption() {
		return option;
	}

	public String getLabel() {
		return label;
	}

}
