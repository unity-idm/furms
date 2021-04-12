/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.project_installation.ProjectInstallationStatus;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("project_installation_job")
public class ProjectInstallationJobEntity extends UUIDIdentifiable {
	public final ProjectInstallationStatus status;
	public final UUID correlationId;

	ProjectInstallationJobEntity(UUID id, ProjectInstallationStatus status, UUID correlationId) {
		this.id = id;
		this.status = status;
		this.correlationId = correlationId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationJobEntity that = (ProjectInstallationJobEntity) o;
		return Objects.equals(id, that.id) &&
			status == that.status &&
			Objects.equals(correlationId, that.correlationId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, status, correlationId);
	}

	@Override
	public String toString() {
		return "ProjectInstallationStatusEntity{" +
			"id=" + id +
			", status=" + status +
			", correlationId=" + correlationId +
			'}';
	}
}
