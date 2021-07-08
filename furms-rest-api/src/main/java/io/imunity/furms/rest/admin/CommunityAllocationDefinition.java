/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import java.util.Objects;

class CommunityAllocationDefinition {
	public final SiteCreditId siteAllocationId;
	public final String name;
	public final ResourceType resourceType;
	public final ResourceAmount amount;

	CommunityAllocationDefinition(SiteCreditId siteAllocationId,
	                              String name,
	                              ResourceType resourceType,
	                              ResourceAmount amount) {
		this.siteAllocationId = siteAllocationId;
		this.name = name;
		this.resourceType = resourceType;
		this.amount = amount;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocationDefinition that = (CommunityAllocationDefinition) o;
		return Objects.equals(siteAllocationId, that.siteAllocationId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(resourceType, that.resourceType)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(siteAllocationId, name, resourceType, amount);
	}

	@Override
	public String toString() {
		return "CommunityAllocationDefinition{" +
				"siteAllocationId=" + siteAllocationId +
				", name='" + name + '\'' +
				", resourceType=" + resourceType +
				", amount=" + amount +
				'}';
	}
}
