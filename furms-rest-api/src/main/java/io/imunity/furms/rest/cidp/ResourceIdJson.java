/*
 * Copyright (c) 2021 Bixbit s.c. All rights reserved.
 *  See LICENSE file for licensing information.
 */

package io.imunity.furms.rest.cidp;

import io.imunity.furms.domain.authz.roles.ResourceId;
import io.imunity.furms.domain.authz.roles.ResourceType;

import java.util.Objects;
import java.util.UUID;

public class ResourceIdJson {
	public final UUID id;
	public final ResourceType type;

	public ResourceIdJson(UUID id, ResourceType type) {
		this.id = id;
		this.type = type;
	}

	public ResourceIdJson(ResourceId resourceId) {
		this(resourceId.id, resourceId.type);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceIdJson that = (ResourceIdJson) o;
		return Objects.equals(id, that.id) && type == that.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override
	public String toString() {
		return "ResourceIdJson{" +
				"id=" + id +
				", type=" + type +
				'}';
	}
}
