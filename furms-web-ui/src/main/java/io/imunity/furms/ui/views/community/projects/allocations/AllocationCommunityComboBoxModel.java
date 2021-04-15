/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;

import java.util.Objects;

class AllocationCommunityComboBoxModel {
	public final String id;
	public final String name;
	public final boolean split;
	public final ResourceMeasureUnit unit;

	AllocationCommunityComboBoxModel(String id, String name, boolean split, ResourceMeasureUnit unit) {
		this.id = id;
		this.name = name;
		this.split = split;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		AllocationCommunityComboBoxModel that = (AllocationCommunityComboBoxModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "AllocationCommunityComboBoxModel{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", unit='" + unit + '\'' +
			'}';
	}
}
