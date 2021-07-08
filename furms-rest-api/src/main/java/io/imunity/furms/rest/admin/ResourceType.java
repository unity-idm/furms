/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ResourceType {
	public final ResourceTypeId id;
	public final String name;
	public final ServiceId serviceId;

	ResourceType(ResourceTypeId id, String name, ServiceId serviceId) {
		this.id = id;
		this.name = name;
		this.serviceId = serviceId;
	}

	ResourceType(io.imunity.furms.domain.resource_types.ResourceType resourceType) {
		this(new ResourceTypeId(resourceType.siteId, resourceType.id),
				resourceType.name,
				new ServiceId(resourceType.siteId, resourceType.serviceId));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceType that = (ResourceType) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(name, that.name)
				&& Objects.equals(serviceId, that.serviceId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, serviceId);
	}

	@Override
	public String toString() {
		return "ResourceType{" +
				"id=" + id +
				", name='" + name + '\'' +
				", serviceId=" + serviceId +
				'}';
	}
}
