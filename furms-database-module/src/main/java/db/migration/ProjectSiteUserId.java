/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package db.migration;

import java.util.Objects;
import java.util.UUID;

class ProjectSiteUserId {
	public final UUID siteId;
	public final UUID projectId;
	public final String userId;

	ProjectSiteUserId(UUID siteId, UUID projectId, String userId) {
		this.siteId = siteId;
		this.projectId = projectId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectSiteUserId that = (ProjectSiteUserId) o;
		return Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, projectId, userId);
	}

	@Override
	public String toString() {
		return "ProjectSiteUserId{" +
			"siteId=" + siteId +
			", projectId=" + projectId +
			", userId=" + userId +
			'}';
	}
}
