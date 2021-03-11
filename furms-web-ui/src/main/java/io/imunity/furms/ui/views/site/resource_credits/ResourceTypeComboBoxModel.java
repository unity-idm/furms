/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_credits;

import io.imunity.furms.domain.resource_types.Unit;

import java.util.Objects;

class ResourceTypeComboBoxModel {
	public final String id;
	public final String name;
	public final Unit unit;

	public ResourceTypeComboBoxModel(String id, String name, Unit unit) {
		this.id = id;
		this.name = name;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceTypeComboBoxModel that = (ResourceTypeComboBoxModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ResourceTypeComboBoxModel{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", unit='" + unit + '\'' +
			'}';
	}
}
