/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_types;

import java.util.Objects;

public class ResourceTypeUpdatedEvent implements ResourceTypeEvent {
	public final ResourceType oldResourceType;
	public final ResourceType newResourceType;

	public ResourceTypeUpdatedEvent(ResourceType oldResourceType, ResourceType newResourceType) {
		this.oldResourceType = oldResourceType;
		this.newResourceType = newResourceType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceTypeUpdatedEvent that = (ResourceTypeUpdatedEvent) o;
		return Objects.equals(oldResourceType, that.oldResourceType) &&
			Objects.equals(newResourceType, that.newResourceType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldResourceType, newResourceType);
	}

	@Override
	public String toString() {
		return "ResourceTypeUpdatedEvent{" +
			"oldResourceType='" + oldResourceType + '\'' +
			"newResourceType='" + newResourceType + '\'' +
			'}';
	}
}
