/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.community.projects.allocations;

import java.util.Objects;

class ResourceTypeComboBoxModel {
	public final String id;
	public final String name;

	public ResourceTypeComboBoxModel(String id, String name) {
		this.id = id;
		this.name = name;
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
			'}';
	}
}
