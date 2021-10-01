/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.applications;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("application")
class ApplicationEntity extends UUIDIdentifiable {
	public final UUID projectId;
	public final String userId;

	ApplicationEntity(UUID id, UUID projectId, String userId) {
		this.id = id;
		this.projectId = projectId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ApplicationEntity that = (ApplicationEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, userId);
	}

	@Override
	public String toString() {
		return "ApplicationEntity{" +
			"projectId=" + projectId +
			", userId='" + userId + '\'' +
			", id=" + id +
			'}';
	}
}
