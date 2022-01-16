/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation;

import java.util.Objects;

public class ProjectAllocationRemovedEvent implements ProjectAllocationEvent {
	public final ProjectAllocation projectAllocation;

	public ProjectAllocationRemovedEvent(ProjectAllocation projectAllocation) {
		this.projectAllocation = projectAllocation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationRemovedEvent that = (ProjectAllocationRemovedEvent) o;
		return Objects.equals(projectAllocation, that.projectAllocation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectAllocation);
	}

	@Override
	public String toString() {
		return "RemoveProjectAllocationEvent{" +
			"projectAllocation='" + projectAllocation + '\'' +
			'}';
	}
}
