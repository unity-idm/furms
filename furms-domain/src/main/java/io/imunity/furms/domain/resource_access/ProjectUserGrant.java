/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class ProjectUserGrant {
	public final String projectId;
	public final FenixUserId userId;

	public ProjectUserGrant(String projectId, FenixUserId userId) {
		this.projectId = projectId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUserGrant userGrant = (ProjectUserGrant) o;
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
