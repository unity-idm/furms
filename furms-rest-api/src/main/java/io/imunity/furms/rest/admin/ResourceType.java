/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ResourceType {
	public final String typeId;
	public final String name;
	public final String serviceId;

	ResourceType(String typeId, String name, String serviceId) {
		this.typeId = typeId;
		this.name = name;
		this.serviceId = serviceId;
	}

	ResourceType(io.imunity.furms.domain.resource_types.ResourceType resourceType) {
		this(resourceType.id.id.toString(), resourceType.name, resourceType.serviceId.id.toString());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceType that = (ResourceType) o;
		return Objects.equals(typeId, that.typeId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(serviceId, that.serviceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(typeId, name, serviceId);
	}

	@Override
	public String toString() {
		return "ResourceType{" +
				"typeId='" + typeId + '\'' +
				", name='" + name + '\'' +
				", serviceId='" + serviceId + '\'' +
				'}';
	}
}
