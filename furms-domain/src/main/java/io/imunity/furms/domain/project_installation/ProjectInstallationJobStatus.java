/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_installation;

import java.util.Objects;

public class ProjectInstallationJobStatus {
	public final String siteName;
	public final String projectId;
	public final ProjectInstallationStatus status;

	public ProjectInstallationJobStatus(String siteName, String projectId, ProjectInstallationStatus status) {
		this.siteName = siteName;
		this.projectId = projectId;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationJobStatus that = (ProjectInstallationJobStatus) o;
		return status == that.status &&
			Objects.equals(siteName, that.siteName) &&
			Objects.equals(projectId, that.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteName, projectId, status);
	}

	@Override
	public String toString() {
		return "ProjectInstallationJobStatus{" +
			"siteName='" + siteName + '\'' +
			", projectId=" + projectId +
			", status=" + status +
			'}';
	}
}
