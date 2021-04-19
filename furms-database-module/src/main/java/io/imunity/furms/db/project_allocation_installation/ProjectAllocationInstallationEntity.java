/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("project_allocation_installation")
class ProjectAllocationInstallationEntity extends UUIDIdentifiable {
	public final UUID correlationId;
	public final UUID siteId;
	public final UUID projectAllocationId;
	public final String chunkId;
	public final ProjectAllocationInstallationStatus status;

	ProjectAllocationInstallationEntity(UUID id, UUID correlationId, UUID siteId, UUID projectAllocationId, String chunkId, ProjectAllocationInstallationStatus status) {
		this.id = id;
		this.correlationId = correlationId;
		this.siteId = siteId;
		this.projectAllocationId = projectAllocationId;
		this.chunkId = chunkId;
		this.status = status;
	}

	ProjectAllocationInstallation toProjectAllocationInstallation() {
		return ProjectAllocationInstallation.builder()
			.id(id.toString())
			.correlationId(correlationId.toString())
			.siteId(siteId.toString())
			.projectAllocationId(projectAllocationId.toString())
			.chunkId(chunkId)
			.status(status)
			.build();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationInstallationEntity that = (ProjectAllocationInstallationEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectAllocationId, that.projectAllocationId) &&
			Objects.equals(chunkId, that.chunkId) && status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, siteId, projectAllocationId, chunkId, status);
	}

	@Override
	public String toString() {
		return "ProjectAllocationJobEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", siteId=" + siteId +
			", projectAllocationId=" + projectAllocationId +
			", chunkId='" + chunkId + '\'' +
			", status=" + status +
			'}';
	}

	public static ProjectAllocationJobEntityBuilder builder() {
		return new ProjectAllocationJobEntityBuilder();
	}

	public static final class ProjectAllocationJobEntityBuilder {
		public UUID correlationId;
		public UUID siteId;
		public UUID projectAllocationId;
		public String chunkId;
		public ProjectAllocationInstallationStatus status;
		protected UUID id;

		private ProjectAllocationJobEntityBuilder() {
		}

		public ProjectAllocationJobEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationJobEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectAllocationJobEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectAllocationJobEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public ProjectAllocationJobEntityBuilder chunkId(String chunkId) {
			this.chunkId = chunkId;
			return this;
		}

		public ProjectAllocationJobEntityBuilder status(ProjectAllocationInstallationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectAllocationInstallationEntity build() {
			return new ProjectAllocationInstallationEntity(id, correlationId, siteId, projectAllocationId, chunkId, status);
		}
	}
}
