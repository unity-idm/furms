/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.community_allocation;

import io.imunity.furms.domain.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CommunityAllocationId implements Id {
	public final UUID id;

	public CommunityAllocationId(UUID id) {
		this.id = id;
	}

	public CommunityAllocationId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public CommunityAllocationId(CommunityAllocationId id) {
		this.id = Optional.ofNullable(id)
			.map(communityAllocationId -> communityAllocationId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationId communityAllocationId = (CommunityAllocationId) o;
		return Objects.equals(id, communityAllocationId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "CommunityAllocationId{" +
			"id=" + id +
			'}';
	}

	@Override
	public UUID getId() {
		return id;
	}
}
