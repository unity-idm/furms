/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.site_agent.CorrelationId;
import io.imunity.furms.domain.sites.SiteId;

import java.util.Objects;

public class ProjectUpdateJob {
	public final ProjectUpdateId id;
	public final SiteId siteId;
	public final ProjectId projectId;
	public final CorrelationId correlationId;
	public final ProjectUpdateStatus status;

	ProjectUpdateJob(ProjectUpdateId id, SiteId siteId, ProjectId projectId, CorrelationId correlationId, ProjectUpdateStatus status) {
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
		private ProjectUpdateId id;
		private SiteId siteId;
		private ProjectId projectId;
		private CorrelationId correlationId;
		private ProjectUpdateStatus status;

		private ProjectUpdateJobBuilder() {
		}

		public ProjectUpdateJobBuilder id(String id) {
			this.id = new ProjectUpdateId(id);
			return this;
		}

		public ProjectUpdateJobBuilder siteId(String siteId) {
			this.siteId = new SiteId(siteId);
			return this;
		}

		public ProjectUpdateJobBuilder projectId(String projectId) {
			this.projectId = new ProjectId(projectId);
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
