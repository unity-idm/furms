/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.services;

import io.imunity.furms.domain.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InfraServiceId implements Id {
	public final UUID id;

	public InfraServiceId(UUID id) {
		this.id = id;
	}

	public InfraServiceId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public InfraServiceId(InfraServiceId id) {
		this.id = Optional.ofNullable(id)
			.map(resourceCreditId -> resourceCreditId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InfraServiceId infraServiceId = (InfraServiceId) o;
		return Objects.equals(id, infraServiceId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "InfraServiceId{" +
			"id=" + id +
			'}';
	}

	@Override
	public UUID getId() {
		return id;
	}
}
