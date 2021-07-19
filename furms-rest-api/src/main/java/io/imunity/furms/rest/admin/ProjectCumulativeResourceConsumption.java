/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;

import java.time.ZonedDateTime;
import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCZoned;

class ProjectCumulativeResourceConsumption {
	public final ProjectAllocationId projectAllocationId;
	public final ResourceTypeId resourceTypeId;
	public final ResourceAmount consumedAmount;
	public final ZonedDateTime consumedUntil;
	
	ProjectCumulativeResourceConsumption(ProjectAllocationId projectAllocationId,
			ResourceTypeId resourceTypeId, ResourceAmount consumedAmount, ZonedDateTime consumedUntil) {
		this.projectAllocationId = projectAllocationId;
		this.resourceTypeId = resourceTypeId;
		this.consumedAmount = consumedAmount;
		this.consumedUntil = consumedUntil;
	}

	public ProjectCumulativeResourceConsumption(ProjectAllocationResolved allocation) {
		this(new ProjectAllocationId(allocation.projectId, allocation.id),
				new ResourceTypeId(allocation.site.getId(), allocation.resourceType.id),
				new ResourceAmount(allocation.consumed, allocation.resourceType.unit.getSuffix()),
				convertToUTCZoned(allocation.resourceCredit.utcEndTime));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectCumulativeResourceConsumption that = (ProjectCumulativeResourceConsumption) o;
		return Objects.equals(projectAllocationId, that.projectAllocationId)
				&& Objects.equals(resourceTypeId, that.resourceTypeId)
				&& Objects.equals(consumedAmount, that.consumedAmount)
				&& Objects.equals(consumedUntil, that.consumedUntil);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectAllocationId, resourceTypeId, consumedAmount, consumedUntil);
	}

	@Override
	public String toString() {
		return "ProjectCumulativeResourceConsumption{" +
				"projectAllocationId=" + projectAllocationId +
				", resourceTypeId=" + resourceTypeId +
				", consumedAmount=" + consumedAmount +
				", consumedUntil=" + consumedUntil +
				'}';
	}

}
