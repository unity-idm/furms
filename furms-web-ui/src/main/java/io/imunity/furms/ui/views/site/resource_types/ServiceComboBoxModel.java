/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.ui.views.site.resource_types;

import io.imunity.furms.domain.services.InfraServiceId;

import java.util.Objects;

class ServiceComboBoxModel {
	public final InfraServiceId id;
	public final String name;

	public ServiceComboBoxModel(InfraServiceId id) {
		this.id = id;
		this.name = null;
	}

	public ServiceComboBoxModel(InfraServiceId id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ServiceComboBoxModel that = (ServiceComboBoxModel) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ServiceComboBoxModel{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			'}';
	}
}
