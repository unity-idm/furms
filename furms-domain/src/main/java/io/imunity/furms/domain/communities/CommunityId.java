/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.communities;

import io.imunity.furms.domain.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class CommunityId implements Id {
	public final UUID id;

	public CommunityId(UUID id) {
		this.id = id;
	}

	public CommunityId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public CommunityId(CommunityId id) {
		this.id = Optional.ofNullable(id)
			.map(communityId -> communityId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityId communityId = (CommunityId) o;
		return Objects.equals(id, communityId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "CommunityId{" +
			"id=" + id +
			'}';
	}

	@Override
	public UUID getId() {
		return id;
	}
}
