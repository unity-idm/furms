/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_allocation_installation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallation;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationInstallationStatus;
import io.imunity.furms.domain.site_agent.CorrelationId;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Table("project_allocation_installation")
class ProjectAllocationInstallationEntity extends UUIDIdentifiable {
	public final UUID correlationId;
	public final UUID siteId;
	public final UUID projectAllocationId;
	public final int status;
	public final String code;
	public final String message;

	ProjectAllocationInstallationEntity(UUID id, UUID correlationId, UUID siteId, UUID projectAllocationId, int status,
	                                    String code, String message) {
		this.id = id;
		this.correlationId = correlationId;
		this.siteId = siteId;
		this.projectAllocationId = projectAllocationId;
		this.status = status;
		this.code = code;
		this.message = message;
	}

	ProjectAllocationInstallation toProjectAllocationInstallation() {
		return ProjectAllocationInstallation.builder()
			.id(id.toString())
			.correlationId(new CorrelationId(correlationId.toString()))
			.siteId(Optional.ofNullable(siteId).map(UUID::toString).orElse(null))
			.projectAllocationId(projectAllocationId.toString())
			.status(ProjectAllocationInstallationStatus.valueOf(status))
			.errorMessage(code, message)
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
			Objects.equals(code, that.code) &&
			Objects.equals(message, that.message) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, siteId, projectAllocationId, code, message, status);
	}

	@Override
	public String toString() {
		return "ProjectAllocationInstallationEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", siteId=" + siteId +
			", projectAllocationId=" + projectAllocationId +
			", code=" + code +
			", message=" + message +
			", status=" + status +
			'}';
	}

	public static ProjectAllocationInstallationEntityBuilder builder() {
		return new ProjectAllocationInstallationEntityBuilder();
	}

	public static final class ProjectAllocationInstallationEntityBuilder {
		protected UUID id;
		public UUID correlationId;
		public UUID siteId;
		public UUID projectAllocationId;
		public int status;
		public String code;
		public String message;

		private ProjectAllocationInstallationEntityBuilder() {
		}

		public ProjectAllocationInstallationEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder projectAllocationId(UUID projectAllocationId) {
			this.projectAllocationId = projectAllocationId;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder status(ProjectAllocationInstallationStatus status) {
			this.status = status.getPersistentId();
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder code(String code) {
			this.code = code;
			return this;
		}

		public ProjectAllocationInstallationEntityBuilder message(String message) {
			this.message = message;
			return this;
		}

		public ProjectAllocationInstallationEntity build() {
			return new ProjectAllocationInstallationEntity(id, correlationId, siteId, projectAllocationId, status, code, message);
		}
	}
}
