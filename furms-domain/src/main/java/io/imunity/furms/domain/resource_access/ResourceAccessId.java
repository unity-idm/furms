/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ResourceAccessId {
	public final UUID id;

	public ResourceAccessId(UUID id) {
		this.id = id;
	}

	public ResourceAccessId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public ResourceAccessId(ResourceAccessId id) {
		this.id = Optional.ofNullable(id)
			.map(resourceCreditId -> resourceCreditId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ResourceAccessId resourceCreditId = (ResourceAccessId) o;
		return Objects.equals(id, resourceCreditId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ResourceAccessId{" +
			"id=" + id +
			'}';
	}
}
