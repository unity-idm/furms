/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.Objects;

public class ResourceTypeCreatedEvent implements ResourceTypeEvent {
	public final ResourceType resourceType;

	public ResourceTypeCreatedEvent(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceTypeCreatedEvent that = (ResourceTypeCreatedEvent) o;
		return Objects.equals(resourceType, that.resourceType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(resourceType);
	}

	@Override
	public String toString() {
		return "ResourceTypeCreatedEvent{" +
			"resourceType='" + resourceType + '\'' +
			'}';
	}
}
