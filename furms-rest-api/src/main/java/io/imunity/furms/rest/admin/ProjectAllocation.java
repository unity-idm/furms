/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;

import java.util.Objects;

class ProjectAllocation extends ProjectAllocationDefinition {
	public final ProjectAllocationId id;

	ProjectAllocation(CommunityAllocationId communityAllocationId, String name, ResourceType resourceType,
			ResourceAmount credits, ProjectAllocationId id) {
		super(communityAllocationId, name, resourceType, credits);
		this.id = id;
	}

	ProjectAllocation(ProjectAllocationResolved allocation) {
		this(new CommunityAllocationId(allocation.communityAllocation.communityId, allocation.communityAllocation.id),
				allocation.name,
				new ResourceType(allocation.resourceType),
				new ResourceAmount(allocation.resourceCredit.amount, allocation.resourceType.unit.getSuffix()),
				new ProjectAllocationId(allocation.projectId, allocation.id));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		ProjectAllocation that = (ProjectAllocation) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id);
	}

	@Override
	public String toString() {
		return "ProjectAllocation{" +
				"id=" + id +
				", communityAllocationId=" + communityAllocationId +
				", name='" + name + '\'' +
				", resourceType=" + resourceType +
				", amount=" + amount +
				'}';
	}
}
