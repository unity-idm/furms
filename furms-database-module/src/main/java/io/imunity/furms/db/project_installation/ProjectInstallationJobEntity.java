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
	public final UUID correlationId;
	public final UUID siteId;
	public final UUID projectId;
	public final ProjectInstallationStatus status;


	ProjectInstallationJobEntity(UUID id, UUID correlationId, UUID siteId, UUID projectId, ProjectInstallationStatus status) {
		this.id = id;
		this.correlationId = correlationId;
		this.siteId = siteId;
		this.projectId = projectId;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationJobEntity that = (ProjectInstallationJobEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, siteId, projectId, status);
	}

	@Override
	public String toString() {
		return "ProjectInstallationJobEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", status=" + status +
			'}';
	}

	public static ProjectInstallationJobEntityBuilder builder() {
		return new ProjectInstallationJobEntityBuilder();
	}

	public static final class ProjectInstallationJobEntityBuilder {
		public UUID correlationId;
		public UUID siteId;
		public UUID projectId;
		public ProjectInstallationStatus status;
		protected UUID id;

		private ProjectInstallationJobEntityBuilder() {
		}

		public ProjectInstallationJobEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ProjectInstallationJobEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectInstallationJobEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectInstallationJobEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectInstallationJobEntityBuilder status(ProjectInstallationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectInstallationJobEntity build() {
			return new ProjectInstallationJobEntity(id, correlationId, siteId, projectId, status);
		}
	}
}
