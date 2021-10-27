/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import io.imunity.furms.db.id.uuid.UUIDIdentifiable;
import io.imunity.furms.domain.project_installation.ProjectUpdateStatus;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Objects;
import java.util.UUID;

@Table("project_update_job")
public class ProjectUpdateJobEntity extends UUIDIdentifiable {
	public final UUID correlationId;
	public final UUID siteId;
	public final UUID projectId;
	public final int status;
	public final String code;
	public final String message;

	ProjectUpdateJobEntity(UUID id, UUID correlationId, UUID siteId, UUID projectId, int status, String code, String message) {
		this.id = id;
		this.correlationId = correlationId;
		this.siteId = siteId;
		this.projectId = projectId;
		this.status = status;
		this.code = code;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUpdateJobEntity that = (ProjectUpdateJobEntity) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(correlationId, that.correlationId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(code, that.code) &&
			Objects.equals(message, that.message) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, siteId, projectId, status, message, code);
	}

	@Override
	public String toString() {
		return "ProjectUpdateJobEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", status=" + status +
			", message=" + message +
			", code=" + code +
			'}';
	}

	public static ProjectUpdateJobEntityBuilder builder() {
		return new ProjectUpdateJobEntityBuilder();
	}

	public static final class ProjectUpdateJobEntityBuilder {
		private UUID id;
		private UUID correlationId;
		private UUID siteId;
		private UUID projectId;
		private int status;
		private String code;
		private String message;

		private ProjectUpdateJobEntityBuilder() {
		}

		public ProjectUpdateJobEntityBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public ProjectUpdateJobEntityBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectUpdateJobEntityBuilder siteId(UUID siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectUpdateJobEntityBuilder projectId(UUID projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectUpdateJobEntityBuilder status(ProjectUpdateStatus status) {
			this.status = status.getPersistentId();
			return this;
		}

		public ProjectUpdateJobEntityBuilder status(int status) {
			this.status = status;
			return this;
		}

		public ProjectUpdateJobEntityBuilder code(String code) {
			this.code = code;
			return this;
		}

		public ProjectUpdateJobEntityBuilder message(String message) {
			this.message = message;
			return this;
		}

		public ProjectUpdateJobEntity build() {
			return new ProjectUpdateJobEntity(id, correlationId, siteId, projectId, status, code, message);
		}
	}
}
