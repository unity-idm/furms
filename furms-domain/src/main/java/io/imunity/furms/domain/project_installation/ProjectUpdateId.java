/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class ProjectUpdateId {
	public final UUID id;

	public ProjectUpdateId(UUID id) {
		this.id = id;
	}

	public ProjectUpdateId(String id) {
		this.id = Optional.ofNullable(id)
			.map(UUID::fromString)
			.orElse(null);
	}

	public ProjectUpdateId(ProjectUpdateId id) {
		this.id = Optional.ofNullable(id)
			.map(projectInstallationId -> projectInstallationId.id)
			.orElse(null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUpdateId projectUpdateId = (ProjectUpdateId) o;
		return Objects.equals(id, projectUpdateId.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "ProjectUpdateId{" +
			"id=" + id +
			'}';
	}
}
