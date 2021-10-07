/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.applications;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;
import java.util.Set;

public class ProjectApplicationRemovedEvent implements ProjectApplicationEvent {
	public final FenixUserId id;
	public final String projectId;
	public final Set<FURMSUser> projectAdmins;

	public ProjectApplicationRemovedEvent(FenixUserId id, String projectId, Set<FURMSUser> projectAdmins) {
		this.id = id;
		this.projectId = projectId;
		this.projectAdmins = Set.copyOf(projectAdmins);
	}

	public boolean isTargetedAt(FURMSUser user) {
		return projectAdmins.stream().anyMatch(adminUsr -> adminUsr.id.equals(user.id));
	}

	@Override
	public FenixUserId getId() {
		return id;
	}

	@Override
	public String getProjectId() {
		return projectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectApplicationRemovedEvent userEvent = (ProjectApplicationRemovedEvent) o;
		return Objects.equals(id, userEvent.id) &&
			Objects.equals(projectId, userEvent.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId);
	}

	@Override
	public String toString() {
		return "ProjectApplicationRemovedEvent{" +
			"id='" + id + '\'' +
			", projectId=" + projectId +
			", projectAdmins=" + projectAdmins +
			'}';
	}
}
