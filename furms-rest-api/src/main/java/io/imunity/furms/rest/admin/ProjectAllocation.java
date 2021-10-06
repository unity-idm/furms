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
	public final String projectId;
	public final String communityAllocationId;
	public final String name;
	public final String resourceTypeId;
	public final String resourceUnit;
	public final String siteId;
	public final String siteName;
	public final String serviceId;
	public final String serviceName;
	public final BigDecimal amount;

	ProjectAllocation(String id,
	                         String projectId,
	                         String communityAllocationId,
	                         String name,
	                         String resourceTypeId,
	                         String resourceUnit,
	                         String siteId,
	                         String siteName,
	                         String serviceId,
	                         String serviceName,
	                         BigDecimal amount) {
		this.id = id;
		this.projectId = projectId;
		this.communityAllocationId = communityAllocationId;
		this.name = name;
		this.resourceTypeId = resourceTypeId;
		this.resourceUnit = resourceUnit;
		this.siteId = siteId;
		this.siteName = siteName;
		this.serviceId = serviceId;
		this.serviceName = serviceName;
		this.amount = amount;
	}

	ProjectAllocation(ProjectAllocationResolved allocation) {
		this(allocation.id, allocation.projectId, allocation.communityAllocation.id, allocation.name, allocation.resourceType.id,
				allocation.resourceType.unit.getSuffix(), allocation.site.getId(), allocation.site.getName(),
				allocation.resourceType.serviceId, allocation.resourceType.serviceName, allocation.amount);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectAllocation that = (ProjectAllocation) o;
		return Objects.equals(id, that.id)
				&& Objects.equals(projectId, that.projectId)
				&& Objects.equals(communityAllocationId, that.communityAllocationId)
				&& Objects.equals(name, that.name)
				&& Objects.equals(resourceTypeId, that.resourceTypeId)
				&& Objects.equals(resourceUnit, that.resourceUnit)
				&& Objects.equals(siteId, that.siteId)
				&& Objects.equals(siteName, that.siteName)
				&& Objects.equals(serviceId, that.serviceId)
				&& Objects.equals(serviceName, that.serviceName)
				&& Objects.equals(amount, that.amount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, projectId, communityAllocationId, name, resourceTypeId, resourceUnit, siteId, siteName,
				serviceId, serviceName, amount);
	}

	@Override
	public String toString() {
		return "ProjectAllocation{" +
				"id='" + id + '\'' +
				", projectId='" + projectId + '\'' +
				", communityAllocationId='" + communityAllocationId + '\'' +
				", name='" + name + '\'' +
				", resourceTypeId='" + resourceTypeId + '\'' +
				", resourceUnit='" + resourceUnit + '\'' +
				", siteId='" + siteId + '\'' +
				", siteName='" + siteName + '\'' +
				", serviceId='" + serviceId + '\'' +
				", serviceName='" + serviceName + '\'' +
				", amount=" + amount +
				'}';
	}
}
