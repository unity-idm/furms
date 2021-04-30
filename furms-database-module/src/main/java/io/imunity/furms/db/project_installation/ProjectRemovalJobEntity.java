/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.project_installation.ProjectRemovalStatus;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("project_removal_job")
public class ProjectRemovalJobEntity extends UUIDIdentifiable {
	public final UUID correlationId;
	public final UUID siteId;
	public final UUID projectId;
	public final ProjectRemovalStatus status;


	ProjectRemovalJobEntity(UUID id, UUID correlationId, UUID siteId, UUID projectId, ProjectRemovalStatus status) {
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
		ProjectRemovalJobEntity that = (ProjectRemovalJobEntity) o;
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
		return "ProjectRemovalJobEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", status=" + status +
			'}';
	}

	public static ProjectRemovalJobEntityBuilder builder() {
		return new ProjectRemovalJobEntityBuilder();
	}

	public static final class ProjectRemovalJobEntityBuilder {
		public UUID correlationId;
		public UUID siteId;
		public UUID projectId;
		public ProjectRemovalStatus status;
		protected UUID id;

		private ProjectRemovalJobEntityBuilder() {
		}

		public ProjectRemovalJobEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ProjectRemovalJobEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectRemovalJobEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectRemovalJobEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectRemovalJobEntityBuilder status(ProjectRemovalStatus status) {
			this.status = status;
			return this;
		}

		public ProjectRemovalJobEntity build() {
			return new ProjectRemovalJobEntity(id, correlationId, siteId, projectId, status);
		}
	}
}
