/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

import java.util.Objects;

class CommunityAllocation extends CommunityAllocationDefinition {
	
	public final CommunityAllocationId id;

	CommunityAllocation(SiteCreditId siteAllocationId, String name, ResourceType resourceType,
			ResourceAmount credits, CommunityAllocationId id) {
		super(siteAllocationId, name, resourceType, credits);
		this.id = id;
	}

	CommunityAllocation(CommunityAllocationResolved allocation) {
		this(new SiteCreditId(allocation.resourceCredit),
				allocation.name,
				new ResourceType(allocation.resourceType),
				new ResourceAmount(allocation.amount, allocation.resourceType.unit.getSuffix()),
				new CommunityAllocationId(allocation.communityId, allocation.id));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CommunityAllocation that = (CommunityAllocation) o;
		return Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id);
	}

	@Override
	public String toString() {
		return "CommunityAllocation{" +
				"id=" + id +
				", siteAllocationId=" + siteAllocationId +
				", name='" + name + '\'' +
				", resourceType=" + resourceType +
				", amount=" + amount +
				'}';
	}
}
