/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import io.imunity.furms.domain.Id;

public class ProjectAllocationId implements Id {
	public final UUID id;

	public ProjectAllocationId(UUID id) {
		this.id = id;
	}

	public ProjectAllocationId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public ProjectAllocationId(ProjectAllocationId id) {
		this.id = Optional.ofNullable(id)
			.map(projectAllocationId -> projectAllocationId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationId projectAllocationId = (ProjectAllocationId) o;
		return Objects.equals(id, projectAllocationId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectAllocationId{" +
			"id=" + id +
			'}';
	}
	
	@Override
	public String asRawString() {
		return RawIdParser.asRawString(id);
	}
}
