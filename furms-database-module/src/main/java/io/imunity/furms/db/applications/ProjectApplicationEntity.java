/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.applications;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;

import java.util.Objects;
import java.util.UUID;

class ProjectApplicationEntity extends UUIDIdentifiable {
	public final UUID projectId;
	public final String projectName;
	public final String userId;

	ProjectApplicationEntity(UUID id, UUID projectId, String userId, String projectName) {
		this.id = id;
		this.projectId = projectId;
		this.projectName = projectName;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectApplicationEntity that = (ProjectApplicationEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectName, that.projectName) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, projectName, userId);
	}

	@Override
	public String toString() {
		return "ApplicationEntity{" +
			"projectId=" + projectId +
			", userId='" + userId + '\'' +
			", projectName='" + projectName + '\'' +
			", id=" + id +
			'}';
	}
}
