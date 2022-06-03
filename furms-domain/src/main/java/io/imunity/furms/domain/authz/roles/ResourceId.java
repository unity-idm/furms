/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.authz.roles;

import io.imunity.furms.domain.Id;

import java.util.Objects;

public class ResourceId {
	public final Id id;
	public final ResourceType type;

	public ResourceId(Id id, ResourceType type) {
		this.id = id;
		this.type = type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceId that = (ResourceId) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(type, that.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type);
	}

	@Override
	public String toString() {
		return "ResourceId{" +
			"id='" + id + '\'' +
			", type='" + type + '\'' +
			'}';
	}
}
