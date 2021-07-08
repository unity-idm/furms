/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import io.imunity.furms.domain.project_allocation_installation.ErrorMessage;

import java.util.Objects;
import java.util.Optional;

public class ProjectInstallationJobStatus {
	public final String siteId;
	public final String siteName;
	public final String projectId;
	public final String gid;
	public final ProjectInstallationStatus status;
	public final Optional<ErrorMessage> errorMessage;

	ProjectInstallationJobStatus(String siteId,
	                             String siteName,
	                             String projectId,
	                             String gid,
	                             ProjectInstallationStatus status,
	                             Optional<ErrorMessage> errorMessage) {
		this.siteId = siteId;
		this.siteName = siteName;
		this.projectId = projectId;
		this.gid = gid;
		this.status = status;
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationJobStatus that = (ProjectInstallationJobStatus) o;
		return status == that.status &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(siteName, that.siteName) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(gid, that.gid) &&
			Objects.equals(errorMessage, that.errorMessage);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, siteName, projectId, status, errorMessage);
	}

	@Override
	public String toString() {
		return "ProjectInstallationJobStatus{" +
			"siteName='" + siteName + '\'' +
			", siteId=" + siteId +
			", projectId=" + projectId +
			", gid=" + gid +
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
		public String gid;
		public ProjectInstallationStatus status;
		public Optional<ErrorMessage> errorMessage = Optional.empty();

		private ProjectInstallationJobStatusBuilder() {
		}

		public ProjectInstallationJobStatusBuilder siteId(String siteId) {
			this.siteId = siteId;
			return this;
		}

		public ProjectInstallationJobStatusBuilder siteName(String siteName) {
			this.siteName = siteName;
			return this;
		}

		public ProjectInstallationJobStatusBuilder projectId(String projectId) {
			this.projectId = projectId;
			return this;
		}

		public ProjectInstallationJobStatusBuilder gid(String gid) {
			this.gid = gid;
			return this;
		}

		public ProjectInstallationJobStatusBuilder status(ProjectInstallationStatus status) {
			this.status = status;
			return this;
		}

		public ProjectInstallationJobStatusBuilder errorMessage(String code, String message) {
			if (code == null && message == null)
				return this;
			this.errorMessage = Optional.of(new ErrorMessage(code, message));
			return this;
		}

		public ProjectInstallationJobStatus build() {
			return new ProjectInstallationJobStatus(siteId, siteName, projectId, gid, status, errorMessage);
		}
	}
}
