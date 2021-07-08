/*
 * Copyright (c) 2020 Bixbit - Krzysztof Benedyczak. All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package io.imunity.furms.rest.admin;

import io.imunity.furms.domain.project_allocation.ProjectAllocationResolved;
import io.imunity.furms.domain.resource_usage.UserResourceUsage;

import java.time.ZonedDateTime;
import java.util.Objects;

import static io.imunity.furms.utils.UTCTimeUtils.convertToUTCZoned;

class ProjectUsageRecord {
	public final ProjectAllocationId projectAllocationId;
	public final ResourceTypeId resourceTypeId;
	public final ResourceAmount consumedAmount;
	public final String userFenixId;
	public final ZonedDateTime from;
	public final ZonedDateTime until;
	
	ProjectUsageRecord(ProjectAllocationId projectAllocationId, ResourceTypeId resourceTypeId,
			ResourceAmount consumedAmount, String userFenixId, ZonedDateTime from, ZonedDateTime until) {
		this.projectAllocationId = projectAllocationId;
		this.resourceTypeId = resourceTypeId;
		this.consumedAmount = consumedAmount;
		this.userFenixId = userFenixId;
		this.from = from;
		this.until = until;
	}

	public ProjectUsageRecord(UserResourceUsage userUsage, ProjectAllocationResolved allocation) {
		this(new ProjectAllocationId(allocation.projectId, allocation.id),
				new ResourceTypeId(allocation.site.getId(), allocation.resourceType.id),
				new ResourceAmount(userUsage.cumulativeConsumption, allocation.resourceType.unit.getSuffix()),
				userUsage.fenixUserId.id,
				convertToUTCZoned(allocation.resourceCredit.utcStartTime),
				convertToUTCZoned(allocation.resourceCredit.utcEndTime));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ProjectUsageRecord that = (ProjectUsageRecord) o;
		return Objects.equals(projectAllocationId, that.projectAllocationId)
				&& Objects.equals(resourceTypeId, that.resourceTypeId)
				&& Objects.equals(consumedAmount, that.consumedAmount)
				&& Objects.equals(userFenixId, that.userFenixId)
				&& Objects.equals(from, that.from)
				&& Objects.equals(until, that.until);
	}

	@Override
	public int hashCode() {
		return Objects.hash(projectAllocationId, resourceTypeId, consumedAmount, userFenixId, from, until);
	}

	@Override
	public String toString() {
		return "ProjectUsageRecord{" +
				"projectAllocationId=" + projectAllocationId +
				", resourceTypeId=" + resourceTypeId +
				", consumedAmount=" + consumedAmount +
				", userFenixId='" + userFenixId + '\'' +
				", from=" + from +
				", until=" + until +
				'}';
	}
}
