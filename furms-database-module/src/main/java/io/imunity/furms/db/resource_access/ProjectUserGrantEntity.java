/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.db.resource_access;

import java.util.Objects;

public class ProjectUserGrantEntity {
	public final String projectId;
	public final String userId;

	public ProjectUserGrantEntity(String projectId, String userId) {
		this.projectId = projectId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUserGrantEntity userGrant = (ProjectUserGrantEntity) o;
		return Objects.equals(projectId, userGrant.projectId) &&
			Objects.equals(userId, userGrant.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, userId);
	}

	@Override
	public String toString() {
		return "UserGrant{" +
			"projectId='" + projectId + '\'' +
			", userId='" + userId + '\'' +
			'}';
	}
}
