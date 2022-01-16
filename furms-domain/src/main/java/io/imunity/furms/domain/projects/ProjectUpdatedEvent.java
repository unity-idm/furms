/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import java.util.Objects;

public class ProjectUpdatedEvent implements ProjectEvent {
	public final Project oldProject;
	public final Project newProject;

	public ProjectUpdatedEvent(Project oldProject, Project newProject) {
		this.oldProject = oldProject;
		this.newProject = newProject;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUpdatedEvent that = (ProjectUpdatedEvent) o;
		return Objects.equals(oldProject, that.oldProject) &&
			Objects.equals(newProject, that.newProject);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldProject, newProject);
	}

	@Override
	public String toString() {
		return "ProjectUpdatedEvent{" +
			"oldProject='" + oldProject + '\'' +
			",newProject='" + newProject + '\'' +
			'}';
	}
}
