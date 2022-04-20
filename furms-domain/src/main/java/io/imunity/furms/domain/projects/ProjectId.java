/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ProjectId {
	public final UUID id;

	public ProjectId(UUID id) {
		this.id = id;
	}

	public ProjectId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public ProjectId(ProjectId id) {
		this.id = Optional.ofNullable(id)
			.map(projectId -> projectId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectId projectId = (ProjectId) o;
		return Objects.equals(id, projectId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectId{" +
			"id=" + id +
			'}';
	}
}
