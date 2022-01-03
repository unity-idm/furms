/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation;

import java.util.Objects;

public class ProjectAllocationUpdatedEvent implements ProjectAllocationEvent {
	public final ProjectAllocation oldProjectAllocation;
	public final ProjectAllocation newProjectAllocation;

	public ProjectAllocationUpdatedEvent(ProjectAllocation oldProjectAllocation, ProjectAllocation newProjectAllocation) {
		this.oldProjectAllocation = oldProjectAllocation;
		this.newProjectAllocation = newProjectAllocation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationUpdatedEvent that = (ProjectAllocationUpdatedEvent) o;
		return Objects.equals(oldProjectAllocation, that.oldProjectAllocation) &&
			Objects.equals(newProjectAllocation, that.newProjectAllocation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(oldProjectAllocation, newProjectAllocation);
	}

	@Override
	public String toString() {
		return "UpdateProjectAllocationEvent{" +
			"oldProjectAllocation='" + oldProjectAllocation + '\'' +
			"newProjectAllocation='" + newProjectAllocation + '\'' +
			'}';
	}
}
