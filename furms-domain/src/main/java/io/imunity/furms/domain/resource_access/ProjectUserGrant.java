/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.resource_access;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class ProjectUserGrant {
	public final String grantId;
	public final String projectId;
	public final FenixUserId userId;

	public ProjectUserGrant(String grantId, String projectId, FenixUserId userId) {
		this.grantId = grantId;
		this.projectId = projectId;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUserGrant that = (ProjectUserGrant) o;
		return Objects.equals(grantId, that.grantId) &&
			Objects.equals(projectId, that.projectId) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(grantId, projectId, userId);
	}

	@Override
	public String toString() {
		return "ProjectUserGrant{" +
			"grantId='" + grantId + '\'' +
			", projectId='" + projectId + '\'' +
			", userId=" + userId +
			'}';
	}
}
