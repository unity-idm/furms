/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.applications;

import io.imunity.furms.domain.users.FURMSUser;

import java.util.Objects;

public class ProjectApplicationWithUser {
	public final String projectId;
	public final String projectName;
	public final FURMSUser user;

	public ProjectApplicationWithUser(String projectId, String projectName, FURMSUser user) {
		this.projectId = projectId;
		this.projectName = projectName;
		this.user = user;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectApplicationWithUser that = (ProjectApplicationWithUser) o;
		return Objects.equals(projectId, that.projectId) &&
			Objects.equals(projectName, that.projectName) &&
			Objects.equals(user, that.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectId, projectName, user);
	}

	@Override
	public String toString() {
		return "ProjectApplication{" +
			"projectId='" + projectId + '\'' +
			", projectName='" + projectName + '\'' +
			", user=" + user +
			'}';
	}
}
