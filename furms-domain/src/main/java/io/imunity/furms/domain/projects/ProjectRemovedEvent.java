/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import io.imunity.furms.domain.users.FURMSUser;

import java.util.List;
import java.util.Objects;

public class ProjectRemovedEvent implements ProjectEvent {
	public final Project project;
	public final List<FURMSUser> projectUsers;

	public ProjectRemovedEvent(List<FURMSUser> projectUsers, Project project) {
		this.projectUsers = projectUsers != null ? List.copyOf(projectUsers) : null;
		this.project = project;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ProjectRemovedEvent that = (ProjectRemovedEvent) o;
		return Objects.equals(projectUsers, that.projectUsers) &&
			Objects.equals(project, that.project);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectUsers, project);
	}

	@Override
	public String toString() {
		return "RemoveProjectEvent{" +
			", project=" + project +
			", projectUsers=" + projectUsers +
			'}';
	}
}
