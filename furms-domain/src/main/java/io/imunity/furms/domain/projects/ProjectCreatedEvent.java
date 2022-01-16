/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import java.util.Objects;

public class ProjectCreatedEvent implements ProjectEvent{
	public final Project project;

	public ProjectCreatedEvent(Project project) {
		this.project = project;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectCreatedEvent that = (ProjectCreatedEvent) o;
		return Objects.equals(project, that.project);
	}

	@Override
	public int hashCode() {
		return Objects.hash(project);
	}

	@Override
	public String toString() {
		return "CreateProjectEvent{" +
			"project='" + project + '\'' +
			'}';
	}
}
