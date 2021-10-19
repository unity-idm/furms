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
	public final int status;
	public final String code;
	public final String message;
	public final String gid;

	ProjectInstallationJobEntity(UUID id, UUID correlationId, UUID siteId, UUID projectId, int status, String code, String message, String gid) {
		this.id = id;
		this.correlationId = correlationId;
		this.siteId = siteId;
		this.projectId = projectId;
		this.code = code;
		this.message = message;
		this.status = status;
		this.gid = gid;
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
			Objects.equals(gid, that.gid) &&
			Objects.equals(code, that.code) &&
			Objects.equals(message, that.message) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, correlationId, siteId, projectId, status, code, message, gid);
	}

	@Override
	public String toString() {
		return "ProjectInstallationJobEntity{" +
			"id=" + id +
			", correlationId=" + correlationId +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", gid=" + gid +
			", status=" + status +
			", code=" + code +
			", message=" + message +
			'}';
	}

	public static ProjectInstallationJobEntityBuilder builder() {
		return new ProjectInstallationJobEntityBuilder();
	}

	public static final class ProjectInstallationJobEntityBuilder {
		private UUID id;
		private UUID correlationId;
		private UUID siteId;
		private UUID projectId;
		private int status;
		private String gid;
		private String code;
		private String message;

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
			this.status = status.getPersistentId();
			return this;
		}

		public ProjectInstallationJobEntityBuilder status(int status) {
			this.status = status;
			return this;
		}

		public ProjectInstallationJobEntityBuilder gid(String gid) {
			this.gid = gid;
			return this;
		}

		public ProjectInstallationJobEntityBuilder code(String code) {
			this.code = code;
			return this;
		}

		public ProjectInstallationJobEntityBuilder message(String message) {
			this.message = message;
			return this;
		}

		public ProjectInstallationJobEntity build() {
			return new ProjectInstallationJobEntity(id, correlationId, siteId, projectId, status, code, message, gid);
		}
	}
}
