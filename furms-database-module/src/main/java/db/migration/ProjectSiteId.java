/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package db.migration;

import java.util.Objects;
import java.util.UUID;

class ProjectSiteId {
	public final UUID siteId;
	public final UUID projectId;

	ProjectSiteId(UUID siteId, UUID projectId) {
		this.siteId = siteId;
		this.projectId = projectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectSiteId that = (ProjectSiteId) o;
		return Objects.equals(siteId, that.siteId) && Objects.equals(projectId, that.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, projectId);
	}

	@Override
	public String toString() {
		return "ProjectSiteId{" +
			"siteId=" + siteId +
			", projectId=" + projectId +
			'}';
	}
}
