/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.project_installation;

import java.util.Objects;
import java.util.UUID;

class ProjectInstallationJobStatusEntity {
	public final UUID siteId;
	public final String siteName;
	public final UUID projectId;
	public final int status;
	public final String gid;
	public final String code;
	public final String message;

	ProjectInstallationJobStatusEntity(UUID siteId,
	                                   String siteName,
	                                   UUID projectId,
	                                   int status,
	                                   String gid,
	                                   String code,
	                                   String message) {
		this.siteId = siteId;
		this.siteName = siteName;
		this.projectId = projectId;
		this.status = status;
		this.gid = gid;
		this.code = code;
		this.message = message;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectInstallationJobStatusEntity that = (ProjectInstallationJobStatusEntity) o;
		return status == that.status &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(siteName, that.siteName) &&
			Objects.equals(code, that.code) &&
			Objects.equals(message, that.message) &&
			Objects.equals(projectId, that.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, siteName, projectId, status, code, message);
	}

	@Override
	public String toString() {
		return "ProjectInstallationJobStatusEntity{" +
			"siteId='" + siteId + '\'' +
			"siteName='" + siteName + '\'' +
			", projectId=" + projectId +
			", status=" + status +
			", code=" + code +
			", message=" + message +
			'}';
	}
}
