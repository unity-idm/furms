/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;

import java.util.Objects;
import java.util.Optional;

public class ProjectUpdateJobStatus {
	public final String siteId;
	public final String projectId;
	public final ProjectUpdateStatus status;
	public final Optional<ErrorMessage> errorMessage;

	ProjectUpdateJobStatus(String siteId, String projectId, ProjectUpdateStatus status, Optional<ErrorMessage> errorMessage) {
		this.siteId = siteId;
		this.projectId = projectId;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUpdateJobStatus that = (ProjectUpdateJobStatus) o;
		return status == that.status &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(errorMessage, that.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, projectId, status, errorMessage);
	}

	@Override
	public String toString() {
		return "ProjectInstallationJobStatus{" +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", status=" + status +
			", errorMessage=" + errorMessage +
			'}';
	}

	public static ProjectInstallationJobStatusBuilder builder() {
		return new ProjectInstallationJobStatusBuilder();
	}

	public static final class ProjectInstallationJobStatusBuilder {
		public String siteId;
		public String siteName;
		public String projectId;
		public ProjectUpdateStatus status;
		public Optional<ErrorMessage> errorMessage = Optional.empty();

		private ProjectInstallationJobStatusBuilder() {
		}

		public ProjectInstallationJobStatusBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectInstallationJobStatusBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectInstallationJobStatusBuilder status(ProjectUpdateStatus status) {
			this.status = status;
			return this;
		}

		public ProjectInstallationJobStatusBuilder errorMessage(String code, String message) {
			if (code == null && message == null)
				return this;
			this.errorMessage = Optional.of(new ErrorMessage(code, message));
			return this;
		}

		public ProjectUpdateJobStatus build() {
			return new ProjectUpdateJobStatus(siteId, projectId, status, errorMessage);
		}
	}
}
