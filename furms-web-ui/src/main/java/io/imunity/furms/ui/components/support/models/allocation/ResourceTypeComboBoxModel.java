/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.components.support.models.allocation;

import io.imunity.furms.domain.resource_types.ResourceMeasureUnit;
import io.imunity.furms.domain.resource_types.ResourceTypeId;

import java.util.Objects;

public class ResourceTypeComboBoxModel {
	public final ResourceTypeId id;
	public final String name;
	public final ResourceMeasureUnit unit;

	public ResourceTypeComboBoxModel(ResourceTypeId id, String name) {
		this.id = id;
		this.name = name;
		this.unit = null;
	}

	public ResourceTypeComboBoxModel(ResourceTypeId id, String name, ResourceMeasureUnit unit) {
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
