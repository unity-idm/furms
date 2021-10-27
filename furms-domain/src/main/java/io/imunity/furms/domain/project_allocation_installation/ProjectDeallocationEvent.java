/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation_installation;

import io.imunity.furms.domain.FurmsEvent;
import io.imunity.furms.domain.resource_access.GrantAccess;

import java.util.Objects;
import java.util.Set;

public class ProjectDeallocationEvent implements FurmsEvent {
	public final Set<GrantAccess> relatedGrantAccesses;

	public ProjectDeallocationEvent(Set<GrantAccess> relatedGrantAccesses) {
		this.relatedGrantAccesses = Set.copyOf(relatedGrantAccesses);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectDeallocationEvent that = (ProjectDeallocationEvent) o;
		return Objects.equals(relatedGrantAccesses, that.relatedGrantAccesses);
	}

	@Override
	public int hashCode() {
		return Objects.hash(relatedGrantAccesses);
	}

	@Override
	public String toString() {
		return "ProjectDeallocationEvent{" +
			"relatedGrantAccesses=" + relatedGrantAccesses +
			'}';
	}
}
