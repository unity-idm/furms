/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.applications;

import io.imunity.furms.domain.users.FURMSUser;
import io.imunity.furms.domain.users.FenixUserId;

import java.util.List;
import java.util.Objects;

public class RemoveProjectApplicationEvent implements ApplicationEvent {
	public final FenixUserId id;
	public final String projectId;
	public final List<FURMSUser> projectAdmins;

	public RemoveProjectApplicationEvent(FenixUserId id, String projectId, List<FURMSUser> projectAdmins) {
		this.id = id;
		this.projectId = projectId;
		this.projectAdmins = projectAdmins;
	}

	public boolean concern(FURMSUser user) {
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
		RemoveProjectApplicationEvent userEvent = (RemoveProjectApplicationEvent) o;
		return Objects.equals(id, userEvent.id) &&
			Objects.equals(projectId, userEvent.projectId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId);
	}

	@Override
	public String toString() {
		return "RemoveApplicationEvent{" +
			"id='" + id + '\'' +
			", projectId=" + projectId +
			'}';
	}
}
