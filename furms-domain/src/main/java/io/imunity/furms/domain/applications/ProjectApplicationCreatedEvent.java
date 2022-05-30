/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.applications;

import io.imunity.furms.domain.projects.ProjectId;
import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.Objects;
import java.util.Set;

public class ProjectApplicationCreatedEvent implements ProjectApplicationEvent {
	public final FenixUserId id;
	public final ProjectId projectId;
	public final Set<FURMSUser> projectAdmins;

	public ProjectApplicationCreatedEvent(FenixUserId id, ProjectId projectId, Set<FURMSUser> projectAdmins) {
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
	public ProjectId getProjectId() {
		return projectId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectApplicationCreatedEvent userEvent = (ProjectApplicationCreatedEvent) o;
		return Objects.equals(id, userEvent.id) &&
			Objects.equals(projectId, userEvent.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId);
	}

	@Override
	public String toString() {
		return "ProjectApplicationCreatedEvent{" +
			"id='" + id + '\'' +
			", projectId=" + projectId +
			", projectAdmins=" + projectAdmins +
			'}';
	}
}
