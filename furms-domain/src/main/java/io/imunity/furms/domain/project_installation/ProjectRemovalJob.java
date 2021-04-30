/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import io.imunity.furms.domain.site_agent.CorrelationId;

import java.util.Objects;

public class ProjectRemovalJob {
	public final String id;
	public final String siteId;
	public final String projectId;
	public final CorrelationId correlationId;
	public final ProjectRemovalStatus status;

	ProjectRemovalJob(String id, String siteId, String projectId, CorrelationId correlationId, ProjectRemovalStatus status) {
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
		ProjectRemovalJob that = (ProjectRemovalJob) o;
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
		return "ProjectRemovalJob{" +
			"id='" + id + '\'' +
			", siteId='" + siteId + '\'' +
			", projectId='" + projectId + '\'' +
			", correlationId=" + correlationId +
			", status=" + status +
			'}';
	}

	public static ProjectRemovalJobBuilder builder() {
		return new ProjectRemovalJobBuilder();
	}

	public static final class ProjectRemovalJobBuilder {
		public String id;
		public String siteId;
		public String projectId;
		public CorrelationId correlationId;
		public ProjectRemovalStatus status;

		private ProjectRemovalJobBuilder() {
		}

		public ProjectRemovalJobBuilder id(String id) {
			this.id = id;
			return this;
		}

		public ProjectRemovalJobBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectRemovalJobBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectRemovalJobBuilder correlationId(CorrelationId correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public ProjectRemovalJobBuilder status(ProjectRemovalStatus status) {
			this.status = status;
			return this;
		}

		public ProjectRemovalJob build() {
			return new ProjectRemovalJob(id, siteId, projectId, correlationId, status);
		}
	}
}
