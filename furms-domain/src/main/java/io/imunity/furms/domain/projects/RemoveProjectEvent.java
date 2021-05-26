/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.projects;

import java.util.List;
import java.util.Objects;

import io.imunity.furms.domain.users.FURMSUser;

public class RemoveProjectEvent implements ProjectEvent {
	public final String id;
	public final List<FURMSUser> projectUsers;

	public RemoveProjectEvent(String id, List<FURMSUser> projectUsers) {
		this.id = id;
		this.projectUsers = projectUsers != null ? List.copyOf(projectUsers) : null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		RemoveProjectEvent that = (RemoveProjectEvent) o;
		return Objects.equals(id, that.id) && Objects.equals(projectUsers, that.projectUsers);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectUsers);
	}

	@Override
	public String toString() {
		return "RemoveProjectEvent{" + "id='" + id + '\'' + ", projectUsers=" + projectUsers + '}';
	}
}
