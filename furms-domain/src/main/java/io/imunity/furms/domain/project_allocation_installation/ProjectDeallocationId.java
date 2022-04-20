/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ProjectDeallocationId {
	public final UUID id;

	public ProjectDeallocationId(UUID id) {
		this.id = id;
	}

	public ProjectDeallocationId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public ProjectDeallocationId(ProjectDeallocationId id) {
		this.id = Optional.ofNullable(id)
			.map(projectDeallocationId -> projectDeallocationId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectDeallocationId projectAllocationInstallationId = (ProjectDeallocationId) o;
		return Objects.equals(id, projectAllocationInstallationId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectDeallocationId{" +
			"id=" + id +
			'}';
	}
}
