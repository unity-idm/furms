/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class ProjectAllocationDefinition {
	public final CommunityAllocationId communityAllocationId;
	public final String name;
	public final ResourceType resourceType;
	public final ResourceAmount amount;

	ProjectAllocationDefinition(CommunityAllocationId communityAllocationId, String name,
			ResourceType resourceType, ResourceAmount amount) {
		this.communityAllocationId = communityAllocationId;
		this.name = name;
		this.resourceType = resourceType;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocationDefinition that = (ProjectAllocationDefinition) o;
		return Objects.equals(communityAllocationId, that.communityAllocationId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(resourceType, that.resourceType)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(communityAllocationId, name, resourceType, amount);
	}

	@Override
	public String toString() {
		return "ProjectAllocationDefinition{" +
				"communityAllocationId=" + communityAllocationId +
				", name='" + name + '\'' +
				", resourceType=" + resourceType +
				", amount=" + amount +
				'}';
	}
}
