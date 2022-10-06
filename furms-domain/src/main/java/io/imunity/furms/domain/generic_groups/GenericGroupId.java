/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.generic_groups;

import java.util.Objects;
import java.util.UUID;

import io.imunity.furms.domain.Id;

public class GenericGroupId implements Id {
	public final UUID id;

	public GenericGroupId(UUID id) {
		this.id = id;
	}

	public GenericGroupId(String id) {
		this.id = UUID.fromString(id);
	}

	public static GenericGroupId empty() {
		return new GenericGroupId((UUID) null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenericGroupId that = (GenericGroupId) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "GenericGroupId{" +
			"id=" + id +
			'}';
	}
	
	@Override
	public String asRawString() {
		return RawIdParser.asRawString(id);
	}
}
