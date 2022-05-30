/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import io.imunity.furms.domain.Id;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ProjectInstallationId implements Id {
	public final UUID id;

	public ProjectInstallationId(UUID id) {
		this.id = id;
	}

	public ProjectInstallationId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public ProjectInstallationId(ProjectInstallationId id) {
		this.id = Optional.ofNullable(id)
			.map(projectInstallationId -> projectInstallationId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationId projectInstallationId = (ProjectInstallationId) o;
		return Objects.equals(id, projectInstallationId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectInstallationId{" +
			"id=" + id +
			'}';
	}

	@Override
	public UUID getId() {
		return id;
	}
}
