/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.applications;

import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;

public class ProjectApplication {
	public final String projectId;
	public final String projectName;
	public final FenixUserId userId;

	public ProjectApplication(String projectId, String projectName, FenixUserId userId) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.userId = userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectApplication that = (ProjectApplication) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectName, that.projectName) &&
			Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, projectName, userId);
	}

	@Override
	public String toString() {
		return "ProjectApplication{" +
			"resourceId='" + projectId + '\'' +
			", resourceName='" + projectName + '\'' +
			", userId=" + userId +
			'}';
	}
}
