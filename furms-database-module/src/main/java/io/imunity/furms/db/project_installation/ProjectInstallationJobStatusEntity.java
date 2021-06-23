/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import java.util.Objects;
import java.util.UUID;

class ProjectInstallationJobStatusEntity {
	public final String siteName;
	public final UUID projectId;
	public final int status;

	ProjectInstallationJobStatusEntity(String siteName, UUID projectId, int status) {
		this.siteName = siteName;
		this.projectId = projectId;
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationJobStatusEntity that = (ProjectInstallationJobStatusEntity) o;
		return status == that.status && Objects.equals(siteName, that.siteName) && Objects.equals(projectId, that.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteName, projectId, status);
	}

	@Override
	public String toString() {
		return "ProjectInstallationJobStatusEntity{" +
			"siteName='" + siteName + '\'' +
			", projectId=" + projectId +
			", status=" + status +
			'}';
	}
}
