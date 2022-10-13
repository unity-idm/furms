/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.domain;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public abstract class UUIDBasedIdentifier implements Id {

	public final UUID id;

	protected UUIDBasedIdentifier(UUID id) {
		this.id = id;
	}

	protected UUIDBasedIdentifier(String id) {
		this(Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null));
	}
	
	protected UUIDBasedIdentifier(UUIDBasedIdentifier id) {
		this(Optional.ofNullable(id)
			.map(uuidBasedId -> uuidBasedId.id)
			.orElse(null));
	}
	
	@Override
	public String asRawString() {
		return id == null ? null : id.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UUIDBasedIdentifier other = (UUIDBasedIdentifier) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "UUIDBasedIdentifier [id=" + id + "]";
	}
}
