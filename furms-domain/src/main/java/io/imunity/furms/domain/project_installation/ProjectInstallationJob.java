/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Objects;

public class ProjectInstallationJob {
	public final String id;
	public final String siteId;
	public final String projectId;
	public final CorrelationId correlationId;
	public final ProjectInstallationStatus status;

	ProjectInstallationJob(String id, String siteId, String projectId, CorrelationId correlationId, ProjectInstallationStatus status) {
		this.id = id;
		this.siteId = siteId;
		this.projectId = projectId;
		this.correlationId = correlationId;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationJob that = (ProjectInstallationJob) o;
		return Objects.equals(id, that.id) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(correlationId, that.correlationId) &&
			status == that.status;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, siteId, projectId, correlationId, status);
	}

	@Override
	public String toString() {
		return "ProjectInstallationJob{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", projectId='" + projectId + '\'' +
			", correlationId=" + correlationId +
			", status=" + status +
			'}';
	}

	public static ProjectInstallationJobBuilder builder() {
		return new ProjectInstallationJobBuilder();
	}

	public static final class ProjectInstallationJobBuilder {
		private String id;
		private String siteId;
		private String projectId;
		private CorrelationId correlationId;
		private ProjectInstallationStatus status;

		private ProjectInstallationJobBuilder() {
		}

		public ProjectInstallationJobBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectInstallationJobBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectInstallationJobBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectInstallationJobBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectInstallationJobBuilder status(ProjectInstallationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectInstallationJob build() {
			return new ProjectInstallationJob(id, siteId, projectId, correlationId, status);
		}
	}
}
