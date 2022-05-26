/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import io.imunity.furms.domain.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class GrantId implements Id {
	public final UUID id;

	public GrantId(UUID id) {
		this.id = id;
	}

	public GrantId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public GrantId(GrantId id) {
		this.id = Optional.ofNullable(id)
			.map(resourceCreditId -> resourceCreditId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GrantId grantId = (GrantId) o;
		return Objects.equals(id, grantId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "GrantId{" +
			"id=" + id +
			'}';
	}

	@Override
	public UUID getId() {
		return id;
	}
}
