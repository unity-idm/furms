/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Objects;

public class ProjectUpdateJob {
	public final String id;
	public final String siteId;
	public final String projectId;
	public final CorrelationId correlationId;
	public final ProjectUpdateStatus status;

	ProjectUpdateJob(String id, String siteId, String projectId, CorrelationId correlationId, ProjectUpdateStatus status) {
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
		ProjectUpdateJob that = (ProjectUpdateJob) o;
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
		return "ProjectUpdateStatus{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", projectId='" + projectId + '\'' +
			", correlationId=" + correlationId +
			", status=" + status +
			'}';
	}

	public static ProjectUpdateJobBuilder builder() {
		return new ProjectUpdateJobBuilder();
	}

	public static final class ProjectUpdateJobBuilder {
		public String id;
		public String siteId;
		public String projectId;
		public CorrelationId correlationId;
		public ProjectUpdateStatus status;

		private ProjectUpdateJobBuilder() {
		}

		public ProjectUpdateJobBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectUpdateJobBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectUpdateJobBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectUpdateJobBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectUpdateJobBuilder status(ProjectUpdateStatus status) {
			this.status = status;
			return this;
		}

		public ProjectUpdateJob build() {
			return new ProjectUpdateJob(id, siteId, projectId, correlationId, status);
		}
	}
}
