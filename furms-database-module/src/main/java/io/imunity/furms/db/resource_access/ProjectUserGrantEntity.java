/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import java.util.Objects;

public class ProjectUserGrantEntity {
	public final String siteId;
	public final String grantId;
	public final String projectId;
	public final String userId;

	ProjectUserGrantEntity(String siteId, String grantId, String projectId, String userId) {
		this.siteId = siteId;
		this.grantId = grantId;
		this.projectId = projectId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUserGrantEntity that = (ProjectUserGrantEntity) o;
		return Objects.equals(grantId, that.grantId) &&
			Objects.equals(siteId, that.siteId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteId, grantId, projectId, userId);
	}

	@Override
	public String toString() {
		return "ProjectUserGrantEntity{" +
			"grantId='" + grantId + '\'' +
			", siteId='" + siteId + '\'' +
			", projectId='" + projectId + '\'' +
			", userId='" + userId + '\'' +
			'}';
	}
}
