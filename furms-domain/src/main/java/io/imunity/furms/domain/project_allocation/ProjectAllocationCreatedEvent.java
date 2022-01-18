/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */

package io.imunity.furms.domain.project_allocation;

import java.util.Objects;

public class ProjectAllocationCreatedEvent implements ProjectAllocationEvent {
	public final ProjectAllocation projectAllocation;

	public ProjectAllocationCreatedEvent(ProjectAllocation projectAllocation) {
		this.projectAllocation = projectAllocation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationCreatedEvent that = (ProjectAllocationCreatedEvent) o;
		return Objects.equals(projectAllocation, that.projectAllocation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectAllocation);
	}

	@Override
	public String toString() {
		return "ProjectAllocationCreatedEvent{" +
			"projectAllocation='" + projectAllocation + '\'' +
			'}';
	}
}
