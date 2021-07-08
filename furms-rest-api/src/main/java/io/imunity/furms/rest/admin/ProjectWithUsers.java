/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.List;
import java.util.Objects;

class ProjectWithUsers {
	public final Project project;
	public final List<String> userFenixUserIds;

	ProjectWithUsers(Project project, List<String> userFenixUserIds) {
		this.project = project;
		this.userFenixUserIds = userFenixUserIds;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectWithUsers that = (ProjectWithUsers) o;
		return Objects.equals(project, that.project)
				&& Objects.equals(userFenixUserIds, that.userFenixUserIds);
	}

	@Override
	public int hashCode() {
		return Objects.hash(project, userFenixUserIds);
	}

	@Override
	public String toString() {
		return "ProjectWithUsers{" +
				"project=" + project +
				", userFenixUserIds=" + userFenixUserIds +
				'}';
	}
}
