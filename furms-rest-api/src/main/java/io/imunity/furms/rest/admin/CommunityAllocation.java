/*
 * Copyright (c) 2020 Bixbit s.c. All rights reserved.
 * See LICENSE file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.community_allocation.CommunityAllocationResolved;

import java.math.BigDecimal;
import java.util.Objects;

class CommunityAllocation {
	public final String id;
	public final String name;
	public final String creditId;
	public final String resourceUnit;
	public final String siteId;
	public final String siteName;
	public final String serviceId;
	public final String serviceName;
	public final BigDecimal amount;

	public CommunityAllocation(String id,
	                           String name,
	                           String creditId,
	                           String resourceUnit,
	                           String siteId,
	                           String siteName,
	                           String serviceId,
	                           String serviceName,
	                           BigDecimal amount) {
		this.id = id;
		this.name = name;
		this.creditId = creditId;
		this.resourceUnit = resourceUnit;
		this.siteId = siteId;
		this.siteName = siteName;
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.amount = amount;
	}

	CommunityAllocation(CommunityAllocationResolved communityAllocation) {
		this(communityAllocation.id.id.toString(), communityAllocation.name,
				communityAllocation.resourceCredit.id.id.toString(), communityAllocation.resourceType.unit.getSuffix(),
				communityAllocation.site.getId().id.toString(), communityAllocation.site.getName(),
				communityAllocation.resourceType.serviceId.id.toString(), communityAllocation.resourceType.serviceName,
				communityAllocation.remaining);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CommunityAllocation that = (CommunityAllocation) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(name, that.name)
				&& Objects.equals(creditId, that.creditId)
				&& Objects.equals(resourceUnit, that.resourceUnit)
				&& Objects.equals(siteId, that.siteId)
				&& Objects.equals(siteName, that.siteName)
				&& Objects.equals(serviceId, that.serviceId)
				&& Objects.equals(serviceName, that.serviceName)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, creditId, resourceUnit, siteId, siteName, serviceId, serviceName, amount);
	}

	@Override
	public String toString() {
		return "CommunityAllocation{" +
				"id='" + id + '\'' +
				", name='" + name + '\'' +
				", creditId='" + creditId + '\'' +
				", resourceUnit='" + resourceUnit + '\'' +
				", siteId='" + siteId + '\'' +
				", siteName='" + siteName + '\'' +
				", serviceId='" + serviceId + '\'' +
				", serviceName='" + serviceName + '\'' +
				", amount=" + amount +
				'}';
	}
}
