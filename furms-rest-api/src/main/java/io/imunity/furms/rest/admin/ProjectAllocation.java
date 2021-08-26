/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;

import java.math.BigDecimal;
import java.util.Objects;

class ProjectAllocation {
	public final String id;
	public final String communityAllocationId;
	public final String name;
	public final String resourceTypeId;
	public final BigDecimal amount;

	ProjectAllocation(String id, String communityAllocationId, String name, String resourceTypeId, BigDecimal amount) {
		this.id = id;
		this.communityAllocationId = communityAllocationId;
		this.name = name;
		this.resourceTypeId = resourceTypeId;
		this.amount = amount;
	}

	ProjectAllocation(ProjectAllocationResolved allocation) {
		this(allocation.id, allocation.communityAllocation.id, allocation.name, allocation.resourceType.id, allocation.amount);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocation that = (ProjectAllocation) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(communityAllocationId, that.communityAllocationId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(resourceTypeId, that.resourceTypeId)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, communityAllocationId, name, resourceTypeId, amount);
	}

	@Override
	public String toString() {
		return "ProjectAllocation{" +
				"id=" + id +
				", communityAllocationId=" + communityAllocationId +
				", name='" + name + '\'' +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", amount=" + amount +
				'}';
	}
}
