/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.project_allocation_installation.ProjectAllocationChunkResolved;

import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCZoned;

class SiteAllocatedResources {
	public final ProjectAllocationId allocationId;
	public final ResourceAmount amount;
	public final Validity validity;
	
	SiteAllocatedResources(ProjectAllocationId allocationId, ResourceAmount amount, Validity validity) {
		this.allocationId = allocationId;
		this.amount = amount;
		this.validity = validity;
	}

	public SiteAllocatedResources(ProjectAllocationResolved projectAllocation) {
		this(new ProjectAllocationId(projectAllocation.projectId, projectAllocation.id),
				new ResourceAmount(projectAllocation.amount, projectAllocation.resourceType.unit.getSuffix()),
				new Validity(convertToUTCZoned(projectAllocation.resourceCredit.utcCreateTime),
							convertToUTCZoned(projectAllocation.resourceCredit.utcEndTime)));
	}

	public SiteAllocatedResources(ProjectAllocationChunkResolved chunk) {
		this(new ProjectAllocationId(chunk.projectAllocation.projectId, chunk.projectAllocation.id),
				new ResourceAmount(chunk.amount, chunk.projectAllocation.resourceType.unit.getSuffix()),
				new Validity(convertToUTCZoned(chunk.validFrom), convertToUTCZoned(chunk.validTo)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SiteAllocatedResources that = (SiteAllocatedResources) o;
		return Objects.equals(allocationId, that.allocationId)
				&& Objects.equals(amount, that.amount)
				&& Objects.equals(validity, that.validity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(allocationId, amount, validity);
	}

	@Override
	public String toString() {
		return "SiteAllocatedResources{" +
				"allocationId=" + allocationId +
				", amount=" + amount +
				", validity=" + validity +
				'}';
	}
}
